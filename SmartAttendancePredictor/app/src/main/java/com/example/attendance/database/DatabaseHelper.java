package com.example.attendance.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.attendance.model.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper class for managing subject attendance data.
 * Handles database creation, version management, and CRUD operations.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "attendance.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    public static final String TABLE_SUBJECTS = "subjects";

    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TOTAL = "total";
    public static final String COLUMN_ATTENDED = "attended";

    // Create table SQL
    private static final String CREATE_TABLE_SUBJECTS =
            "CREATE TABLE " + TABLE_SUBJECTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_TOTAL + " INTEGER NOT NULL, " +
                    COLUMN_ATTENDED + " INTEGER NOT NULL" +
                    ")";

    private static DatabaseHelper instance;

    /**
     * Get singleton instance of DatabaseHelper
     * @param context Application context
     * @return DatabaseHelper instance
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Constructor
     * @param context Application context
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SUBJECTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECTS);
        onCreate(db);
    }

    /**
     * Insert a new subject into the database
     * @param subject Subject object to insert
     * @return true if successful, false otherwise
     */
    public boolean insertSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, subject.getName());
        values.put(COLUMN_TOTAL, subject.getTotal());
        values.put(COLUMN_ATTENDED, subject.getAttended());

        long result = db.insert(TABLE_SUBJECTS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Get all subjects from the database
     * @return List of all subjects
     */
    public List<Subject> getAllSubjects() {
        List<Subject> subjectList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SUBJECTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Subject subject = new Subject();
                subject.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                subject.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                subject.setTotal(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL)));
                subject.setAttended(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ATTENDED)));
                subjectList.add(subject);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return subjectList;
    }

    /**
     * Get a subject by ID
     * @param id Subject ID
     * @return Subject object or null if not found
     */
    public Subject getSubjectById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_SUBJECTS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_TOTAL, COLUMN_ATTENDED},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Subject subject = null;
        if (cursor != null && cursor.moveToFirst()) {
            subject = new Subject(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ATTENDED))
            );
            cursor.close();
        }

        db.close();
        return subject;
    }

    /**
     * Update an existing subject
     * @param subject Subject object with updated data
     * @return true if successful, false otherwise
     */
    public boolean updateSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, subject.getName());
        values.put(COLUMN_TOTAL, subject.getTotal());
        values.put(COLUMN_ATTENDED, subject.getAttended());

        int result = db.update(
                TABLE_SUBJECTS,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(subject.getId())}
        );
        db.close();
        return result > 0;
    }

    /**
     * Delete a subject by ID
     * @param id Subject ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteSubject(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SUBJECTS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    /**
     * Get total number of subjects
     * @return Count of subjects
     */
    public int getSubjectCount() {
        String countQuery = "SELECT * FROM " + TABLE_SUBJECTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}