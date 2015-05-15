package com.example.shridevi.todo;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TaskerDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "taskerManager.db";

    // tasks table name
    private static final String TABLE_TASKS = "tasks";

    // tasks Table Columns names
    private static final String KEY_ID = "_ID";
    private static final String KEY_TASKNAME = "taskName";
    private static final String KEY_STATUS = "status";
    private static final String KEY_DATETIME = "dateTime";

    public TaskerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i("DBH", "TaskerDbHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.i("DBH", "onCreate");

        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_TASKS + " ( "
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TASKNAME + " TEXT,"
                + KEY_STATUS + " INTEGER,"
                + KEY_DATETIME + " TEXT"
                + ");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        // Create tables again
        onCreate(db);
    }

    // Adding new task
    public void addTask(Task task) {
        Log.i("DBH", "addTask");

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASKNAME, task.getTaskName()); // task name
        // status of task- can be 0 for not done and 1 for done
        values.put(KEY_STATUS, task.getStatus());
        values.put(KEY_DATETIME, task.getDateTime());
        // Inserting Row
        db.insert(TABLE_TASKS, null, values);
    }

    public List<Task> getAllUnCheckedTasks() {
        Log.i("DBH", "getAllUnCheckedTasks");

        List<Task> taskList = new ArrayList<Task>();
        SQLiteDatabase db = this.getWritableDatabase();

        String[] projection = {
                KEY_ID,
                KEY_TASKNAME,
                KEY_STATUS,
                KEY_DATETIME
        };

        String selection = KEY_STATUS + " LIKE ?";
        String[] selectionArgs = { String.valueOf(0) };

        Cursor cursor = db.query(
                TABLE_TASKS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTaskName(cursor.getString(1));
                task.setStatus(cursor.getInt(2));
                task.setDateTime(cursor.getString(3));
                // Adding contact to list
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        // return task list
        return taskList;
    }

    public List<Task> getAllTasks() {
        Log.i("DBH", "getAllTask");

        List<Task> taskList = new ArrayList<Task>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTaskName(cursor.getString(1));
                task.setStatus(cursor.getInt(2));
                task.setDateTime(cursor.getString(3));
                // Adding contact to list
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        // return task list
        return taskList;
    }

    public void updateTask(Task task) {
        // updating row
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASKNAME, task.getTaskName());
        values.put(KEY_STATUS, task.getStatus());
        values.put(KEY_DATETIME, task.getDateTime());
        db.update(TABLE_TASKS, values, KEY_ID + " = ?",new String[] {String.valueOf(task.getId())});
    }

    public void deleteTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?",new String[] {String.valueOf(task.getId())});
    }

    public Task getTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] projection = {
                KEY_ID,
                KEY_TASKNAME,
                KEY_STATUS,
                KEY_DATETIME
        };

        String selection = KEY_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(taskId) };

        Cursor c = db.query(
                TABLE_TASKS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (c != null) {
            if(c.moveToFirst()) {
                Log.i("Detail", "read from database count " + c.getCount());

                if (c.getCount() != 1) {
                    Log.w("Detail", "Found more than 1 entries");
                }

                Task task = new Task();
                task.setId(c.getInt(0));
                task.setTaskName(c.getString(1));
                task.setStatus(c.getInt(2));
                task.setDateTime(c.getString(3));

                return task;
            }
        }
        return null;
    }
}
