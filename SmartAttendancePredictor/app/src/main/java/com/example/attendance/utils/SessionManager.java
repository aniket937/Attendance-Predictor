package com.example.attendance.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Session Manager for handling student login session using SharedPreferences.
 * Stores and retrieves login information across app restarts.
 */
public class SessionManager {

    private static final String PREF_NAME = "SmartAttendancePrefs";
    private static final String KEY_ROLL_NO = "roll_no";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_STUDENT_NAME = "student_name";
    private static final String KEY_STUDENT_BRANCH = "student_branch";

    private final Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Save student session after successful login
     */
    public void createLoginSession(int rollNo, String name, String branch) {
        editor.putInt(KEY_ROLL_NO, rollNo);
        editor.putString(KEY_STUDENT_NAME, name);
        editor.putString(KEY_STUDENT_BRANCH, branch);
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.commit();
    }

    /**
     * Check if user is already logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    /**
     * Get current logged in student's roll number
     */
    public int getRollNo() {
        return sharedPreferences.getInt(KEY_ROLL_NO, -1);
    }

    /**
     * Get current logged in student's name
     */
    public String getStudentName() {
        return sharedPreferences.getString(KEY_STUDENT_NAME, "");
    }

    /**
     * Get current logged in student's branch
     */
    public String getStudentBranch() {
        return sharedPreferences.getString(KEY_STUDENT_BRANCH, "");
    }

    /**
     * Logout - clear all session data
     */
    public void logout() {
        editor.clear();
        editor.commit();
    }
}