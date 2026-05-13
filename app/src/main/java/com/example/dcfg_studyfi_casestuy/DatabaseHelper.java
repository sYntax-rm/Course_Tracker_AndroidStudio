package com.example.dcfg_studyfi_casestuy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Configuration (Pinalitan ko ang Version to 2 para mag-trigger ang onUpgrade)
    private static final String DATABASE_NAME = "StudyFi.db";
    private static final int DATABASE_VERSION = 2;

    // Table para sa Courses (Folders)
    private static final String TABLE_COURSES = "courses";
    private static final String COL_COURSE_ID = "id";
    private static final String COL_COURSE_NAME = "course_name";
    private static final String COL_COURSE_COLOR = "color_hex";

    // Table para sa Tasks (Bagong Table)
    private static final String TABLE_TASKS = "tasks";
    private static final String COL_TASK_ID = "task_id";
    private static final String COL_TASK_COURSE_ID = "course_id"; // Foreign Key
    private static final String COL_TASK_NAME = "task_name";
    private static final String COL_TASK_STATUS = "is_completed";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Paggawa ng Course Table
        String createTableCourses = "CREATE TABLE " + TABLE_COURSES + " (" +
                COL_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_COURSE_NAME + " TEXT, " +
                COL_COURSE_COLOR + " TEXT)";
        db.execSQL(createTableCourses);

        // Paggawa ng Tasks Table
        String createTableTasks = "CREATE TABLE " + TABLE_TASKS + " (" +
                COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TASK_COURSE_ID + " INTEGER, " +
                COL_TASK_NAME + " TEXT, " +
                COL_TASK_STATUS + " INTEGER DEFAULT 0)"; // 0 = False, 1 = True
        db.execSQL(createTableTasks);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Kapag nag-update ang version, idagdag ang bagong table kung wala pa
        if (oldVersion < 2) {
            String createTableTasks = "CREATE TABLE IF NOT EXISTS " + TABLE_TASKS + " (" +
                    COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TASK_COURSE_ID + " INTEGER, " +
                    COL_TASK_NAME + " TEXT, " +
                    COL_TASK_STATUS + " INTEGER DEFAULT 0)";
            db.execSQL(createTableTasks);
        }
    }

    // ==================== COURSE METHODS ====================
    public boolean addCourse(String name, String color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COURSE_NAME, name);
        values.put(COL_COURSE_COLOR, color);
        long result = db.insert(TABLE_COURSES, null, values);
        return result != -1;
    }

    public List<Course> getAllCourses() {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSES, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String color = cursor.getString(2);
                courseList.add(new Course(id, name, color));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return courseList;
    }

    // ==================== TASK METHODS ====================

    // Mag-add ng task sa loob ng specific na course
    public boolean addTask(int courseId, String taskName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_COURSE_ID, courseId);
        values.put(COL_TASK_NAME, taskName);
        values.put(COL_TASK_STATUS, 0); // Default ay hindi pa completed
        long result = db.insert(TABLE_TASKS, null, values);
        return result != -1;
    }

    // Kunin lahat ng tasks na kabilang LANG sa isang specific na course
    public List<Task> getTasksForCourse(int courseId) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE " + COL_TASK_COURSE_ID + " = ?", new String[]{String.valueOf(courseId)});

        if (cursor.moveToFirst()) {
            do {
                int taskId = cursor.getInt(0);
                int cId = cursor.getInt(1);
                String taskName = cursor.getString(2);
                boolean isCompleted = cursor.getInt(3) == 1; // 1 = true
                taskList.add(new Task(taskId, cId, taskName, isCompleted));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    // Update kung completed o hindi ang task
    public void updateTaskStatus(int taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_STATUS, isCompleted ? 1 : 0);
        db.update(TABLE_TASKS, values, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    // Burahin ang task
    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }
}