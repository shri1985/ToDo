package com.example.shridevi.todo;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmReciever extends BroadcastReceiver {

    private static int mID = 0;

    public AlarmReciever() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {


        Bundle bundle = intent.getExtras();
        String taskName = bundle.getString(DetailActivity.TASK_TEXT);

        Log.i("AlarmReceiver" , " onRecieve");

        Intent broadcastIntent = new Intent(MainActivity.ACTION_SHOW_NOTIFICATION);
        broadcastIntent.putExtra(DetailActivity.TASK_TEXT, taskName);
        context.sendBroadcast(broadcastIntent);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notify_status)
                        .setContentTitle("ToDo")
                        .setContentText(taskName)
                        .setSound(soundUri);


        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        // Build the notification:
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;


        // Get the notification manager:
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Publish the notification:
        Log.i("AlarmReciver", "notification Id " + mID + " task name " + taskName);
        notificationManager.notify(mID, notification);
        mID = mID + 1;

    }

    public static PendingIntent setAlarm(Context context, Calendar calendar, String taskName, int taskId) {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReciever.class);
        intent.putExtra(DetailActivity.TASK_TEXT, taskName);
        alarmIntent = PendingIntent.getBroadcast(context, taskId, intent, 0);

        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), alarmIntent);

        Log.i("Alarm", "setAlarm " + taskName + " taskId " + taskId);

        return alarmIntent;
    }

    public static void cancelAlarm(Context context, String taskName, int taskId) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReciever.class);
        intent.putExtra(DetailActivity.TASK_TEXT, taskName);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.cancel(pendingIntent);
    }

    public static void cancelAllNotifications(Context context) {

        // Get the notification manager:
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();
    }
}
