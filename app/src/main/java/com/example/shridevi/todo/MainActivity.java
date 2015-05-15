package com.example.shridevi.todo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    protected TaskerDbHelper db;
    public static final String TAG = "Main";

    List<Task> list;
    MyAdapter adapt;
    DelayTask delayTask;

    public Context context;

    public static final String ACTION_SHOW_NOTIFICATION =
            "com.example.shridevi.todo.SHOW_NOTIFICATION";

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, " mOnShowNotification");

            Bundle bundle = intent.getExtras();
            String taskName = bundle.getString(DetailActivity.TASK_TEXT);

            new AlertDialog.Builder(MainActivity.this)
                    .setIcon(R.drawable.ic_notify_status)
                    .setTitle("ToDo")
                    .setMessage(taskName)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }

                    })
                    .show();
        }
    };

    AdapterView.OnItemClickListener clickListener =  new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //item click is one less then DB
            Log.d(TAG, "onItemClick i " + i);
            if (list != null) {
                Task current = list.get(i);
                Log.i(TAG, "DB id: "+ current.getId());
                String selection = current.getTaskName();

                Intent intent = new Intent(MainActivity.this,DetailActivity.class);
                intent.putExtra(DetailActivity.TASK_TEXT,selection);
                intent.putExtra(DetailActivity.TASK_ID, current.getId());
                startActivity(intent);
                db.close();
            }
        }
    };

    private class DelayTask extends AsyncTask<Void,Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.i("debug", "sleeping");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                // sleep can be interrupted, ok
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i("debug", "onPostExecute");
            updateListView();
            delayTask = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        db = new TaskerDbHelper(this);
        updateListView();

        IntentFilter filter = new IntentFilter(ACTION_SHOW_NOTIFICATION);
        registerReceiver(mOnShowNotification, filter);
        delayTask = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (db != null) {
            db.close();
        }
        unregisterReceiver(mOnShowNotification);
        Log.i("debug", "onPause");
        if (delayTask != null) {
            Log.i("debug", "delayTask cancel");
            delayTask.cancel(true);
            delayTask = null;
        }
    }

    public void updateListView() {
        list = db.getAllUnCheckedTasks();
        adapt = new MyAdapter(this, R.layout.layout_inner_list, list);
        ListView listTask = (ListView) findViewById(R.id.listView1);
        listTask.setAdapter(adapt);
        listTask.setOnItemClickListener(clickListener);
    }

    public void addTaskNow(View v) {
        EditText t = (EditText) findViewById(R.id.editText1);
        String s = t.getText().toString();
        if (s.equalsIgnoreCase("")) {
            Toast.makeText(this, "enter the task description first!!", Toast.LENGTH_LONG).show();
        } else {
            Task task = new Task(s, 0);
            db.addTask(task);
            t.setText("");
            updateListView();
        }
    }

    private class MyAdapter extends ArrayAdapter<Task> {
        Context context;
        List<Task> taskList=new ArrayList<Task>();
        int layoutResourceId;
        public MyAdapter(Context context, int layoutResourceId,
                         List<Task> objects) {
            super(context, layoutResourceId, objects);
            this.layoutResourceId = layoutResourceId;
            this.taskList = objects;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckBox checkBox = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.layout_inner_list,
                        parent, false);

                checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);

                convertView.setTag(checkBox);

                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        Task changeTask = (Task) cb.getTag(R.string.kWhatTask);
                        TextView dateTimeView = (TextView) cb.getTag(R.string.kWhatTextView);
                        TextView taskNameView = (TextView) cb.getTag(R.string.kWhatTaskNameString);

                        changeTask.setStatus(cb.isChecked() == true ? 1 : 0);
                        db.updateTask(changeTask);
                        AlarmReciever.cancelAlarm(context, changeTask.getTaskName(), changeTask.getId());
                        Toast.makeText(
                                getApplicationContext(),
                                "Clicked on Checkbox: " + cb.getText() + " is "
                                        + cb.isChecked(), Toast.LENGTH_LONG)
                                .show();

                        if (changeTask.getStatus() == 1) {
                            cb.setTextColor(Color.BLUE);
                            dateTimeView.setTextColor(Color.BLUE);
                            taskNameView.setTextColor(Color.BLUE);
                        } else if (changeTask.pastDue()) {
                            cb.setTextColor(Color.RED);
                            dateTimeView.setTextColor(Color.RED);
                            taskNameView.setTextColor(Color.RED);
                        } else {
                            cb.setTextColor(Color.BLACK);
                            dateTimeView.setTextColor(Color.BLACK);
                            taskNameView.setTextColor(Color.BLACK);
                        }

                        if (delayTask == null) {
                            delayTask = new DelayTask();
                            delayTask.execute();
                        }
                    }
                });
            } else {
                checkBox = (CheckBox) convertView.getTag();
            }

            Task current = taskList.get(position);

            checkBox.setChecked(current.getStatus() == 1 ? true : false);
            checkBox.setTag(R.string.kWhatTask, current);
            TextView dateTimeView = (TextView) convertView.findViewById(R.id.dateTime);
            checkBox.setTag(R.string.kWhatTextView, dateTimeView);
            TextView taskNameView = (TextView) convertView.findViewById(R.id.taskName);
            checkBox.setTag(R.string.kWhatTaskNameString, taskNameView);

            taskNameView.setText(current.getTaskName());

            if (current.getDateTime().equals(Task.INVALID_DATE)) {
                dateTimeView.setText("");
            } else {
                dateTimeView.setText(current.getDateTime());
            }

            if (current.getStatus() == 1) {
                checkBox.setTextColor(Color.BLUE);
                dateTimeView.setTextColor(Color.BLUE);
            } else if (current.pastDue()) {
                checkBox.setTextColor(Color.RED);
                dateTimeView.setTextColor(Color.RED);
            } else {
                checkBox.setTextColor(Color.BLACK);
                dateTimeView.setTextColor(Color.BLACK);
            }

            return convertView;
        }
    }

}
