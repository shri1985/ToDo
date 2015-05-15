package com.example.shridevi.todo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("AlarmBootReceiver", "onReceive");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {


            TaskerDbHelper db = new TaskerDbHelper(context);
            List<Task> list = db.getAllUnCheckedTasks();

            for (Task task : list) {
                if (!task.pastDue()) {
                    AlarmReciever.setAlarm(context, task.getCalendar(), task.getTaskName(), task.getId());
                }
            }
            db.close();
        }
    }
}
