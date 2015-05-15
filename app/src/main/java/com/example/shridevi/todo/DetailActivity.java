package com.example.shridevi.todo;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;


public class DetailActivity extends ActionBarActivity {

    public static final String TASK_TEXT = "task_value";
    public static final String TASK_ID = "task_id";

    private Context context;
    protected TaskerDbHelper db;
    int taskId = -1;

    Task currentTask = null;

    Calendar calendarSelected;
    boolean userSelectedDate = false;
    boolean userSelectedTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Bundle extraData = getIntent().getExtras();
        String tName = extraData.getString(TASK_TEXT);
        taskId = extraData.getInt(TASK_ID);

        EditText editText =(EditText)findViewById(R.id.label);
        editText.setText(tName);

        calendarSelected = Calendar.getInstance();
        userSelectedDate = false;
        userSelectedTime = false;

        context = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Detail", "onResume");
        db = new TaskerDbHelper(this);

        if (taskId > -1) {
            currentTask = db.getTask(taskId);
            Calendar taskCalendar = currentTask.getCalendar();
            if (taskCalendar != null) {
                calendarSelected = taskCalendar;
                int hourOfDay = calendarSelected.get(Calendar.HOUR_OF_DAY);
                int minute = calendarSelected.get(Calendar.MINUTE);
                int year = calendarSelected.get(Calendar.YEAR);
                int month = calendarSelected.get(Calendar.MONTH);
                int day = calendarSelected.get(Calendar.DAY_OF_MONTH);


                Button button = (Button) findViewById(R.id.timeBtn);
                button.setText("Selected Time: " + hourOfDay + ":" + String.format("%02d",minute));

                button = (Button) findViewById(R.id.dateBtn);
                button.setText("Selected Date: " + month + "/" + day + "/" + year);
                userSelectedTime = userSelectedDate = true;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Detail", "onPause");
        if (db != null) {
            db.close();
        }
    }

    public void onSave(View view) {
        EditText editText =(EditText)findViewById(R.id.label);
        String task = editText.getText().toString();
        currentTask.setTaskName(task);

        if (userSelectedDate || userSelectedTime) {
            currentTask.setCalendar(calendarSelected);

            PendingIntent pendingIntent = AlarmReciever.setAlarm(
                    context, calendarSelected, currentTask.getTaskName(), currentTask.getId());

        }

        db.updateTask(currentTask);
        Toast.makeText(this, "Task saved!", Toast.LENGTH_LONG).show();

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        finish();
    }

    public void onDelete(View view) {


        new AlertDialog.Builder(this)
                .setTitle(R.string.deleteTitle)
                .setMessage(R.string.deleteMsg)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteTask(currentTask);

                        AlarmReciever.cancelAlarm(context, currentTask.getTaskName(), currentTask.getId());
                        Toast.makeText(context, "Task deleted!", Toast.LENGTH_LONG).show();

                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);


                        finish();
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @SuppressLint("ValidFragment")
    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            int hour = calendarSelected.get(Calendar.HOUR_OF_DAY);
            int minute = calendarSelected.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
           }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            Button button = (Button) findViewById(R.id.timeBtn);
            button.setText("Selected Time: " + hourOfDay + ":" + String.format("%02d",minute));

            Log.i("Detail", "onTimeSet: hourOfDay : " + hourOfDay);

            calendarSelected.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendarSelected.set(Calendar.MINUTE, minute);
            calendarSelected.set(Calendar.SECOND, 0);
            userSelectedTime = true;

            if (!userSelectedDate) {
                int year = calendarSelected.get(Calendar.YEAR);
                int month = calendarSelected.get(Calendar.MONTH);
                int day = calendarSelected.get(Calendar.DAY_OF_MONTH);

                button = (Button) findViewById(R.id.dateBtn);
                button.setText("Selected Date: " + month + "/" + day + "/" + year);
                calendarSelected.set(year, month, day);
            }
        }
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            int year = calendarSelected.get(Calendar.YEAR);
            int month = calendarSelected.get(Calendar.MONTH);
            int day = calendarSelected.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Button button = (Button) findViewById(R.id.dateBtn);
            button.setText("Selected Date: " + month + "/" + day + "/" + year);
            calendarSelected.set(year, month, day);
            userSelectedDate = true;

            if (!userSelectedTime) {
                int hour = calendarSelected.get(Calendar.HOUR_OF_DAY);
                int minute = calendarSelected.get(Calendar.MINUTE);

                button = (Button) findViewById(R.id.timeBtn);
                button.setText("Selected Time: " + hour + ":" + String.format("%02d",minute));
            }
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

}
