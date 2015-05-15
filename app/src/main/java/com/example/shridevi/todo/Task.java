package com.example.shridevi.todo;

import android.app.PendingIntent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by shridevi on 4/29/15.
 */
public class Task {
    public static final String INVALID_DATE = "00-00-0000 00:00";

        private String taskName;
        private int status;
        private int id;
        private Calendar calendar;
        private PendingIntent pendingIntent = null;


        public Task(){
            this.taskName =null;
            this.status=0;
        }
        public Task(String taskName,int status){
            super();
            this.taskName = taskName;
            this.status = status;
        }
        public int getId(){
            return id;
        }
        public void setId(int id){
            this.id =id;
        }
        public String getTaskName() {
            return taskName;
        }
        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }
        public int getStatus(){
            return status;
        }
        public void setStatus(int status){
            this.status = status;
        }
        public Calendar getCalendar() {
            return calendar;
        }
        public void setCalendar(Calendar calendar) {
            this.calendar = calendar;
        }
        public String getDateTime() {
            if (calendar == null) {
                return INVALID_DATE;
            }
            Date taskDate = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
            String date = sdf.format(taskDate);
            return date;
        }
        public void setDateTime(String dateTime) {
            if (dateTime.equals(INVALID_DATE)) {
                return;
            }
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
            Date taskDate;
            try {
                taskDate = sdf.parse(dateTime);
            } catch (ParseException e) {
                // this is expected error as date format will not parse invalid date
                return;
            }
            calendar.setTime(taskDate);
        }

    public boolean pastDue() {

        if (calendar == null) {
            return false;
        }

        Calendar now = Calendar.getInstance();
        return now.after(calendar);
    }

}


