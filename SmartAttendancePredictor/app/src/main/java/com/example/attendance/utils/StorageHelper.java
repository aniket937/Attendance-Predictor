package com.example.attendance.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.attendance.model.Subject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Helper class for managing file storage operations.
 * Handles internal storage for settings and external storage for data export.
 */
public class StorageHelper {

    private static final String TAG = "StorageHelper";
    private static final String PREFS_FILE = "attendance_prefs";
    private static final String KEY_LAST_SUBJECT = "last_subject_id";

    private Context context;

    public StorageHelper(Context context) {
        this.context = context;
    }

    /**
     * Save last opened subject ID to internal storage
     * @param subjectId Subject ID to save
     */
    public void saveLastSubjectId(int subjectId) {
        try {
            File file = new File(context.getFilesDir(), PREFS_FILE);
            PrintWriter writer = new PrintWriter(new FileOutputStream(file));
            writer.println(KEY_LAST_SUBJECT + "=" + subjectId);
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error saving last subject ID", e);
        }
    }

    /**
     * Load last opened subject ID from internal storage
     * @return Last subject ID, or -1 if not found
     */
    public int loadLastSubjectId() {
        try {
            File file = new File(context.getFilesDir(), PREFS_FILE);
            if (file.exists()) {
                java.util.Scanner scanner = new java.util.Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith(KEY_LAST_SUBJECT + "=")) {
                        String value = line.split("=")[1];
                        scanner.close();
                        return Integer.parseInt(value);
                    }
                }
                scanner.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading last subject ID", e);
        }
        return -1;
    }

    /**
     * Export attendance data to CSV file on external storage
     * @param subjects List of subjects to export
     * @return true if successful, false otherwise
     */
    public boolean exportToCSV(List<Subject> subjects) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "External storage not writable");
            return false;
        }

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File exportFile = new File(downloadsDir, "attendance_export.csv");

        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(exportFile));
            writer.println("ID,Subject Name,Total Classes,Classes Attended,Attendance %");

            for (Subject subject : subjects) {
                double percentage = AttendanceUtils.calculateAttendance(
                        subject.getAttended(),
                        subject.getTotal()
                );
                writer.println(String.format("%d,%s,%d,%d,%.2f",
                        subject.getId(),
                        subject.getName(),
                        subject.getTotal(),
                        subject.getAttended(),
                        percentage
                ));
            }

            writer.close();
            Log.i(TAG, "Exported to: " + exportFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error exporting to CSV", e);
            return false;
        }
    }

    /**
     * Check if external storage is writable
     * @return true if writable, false otherwise
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Check if external storage is readable
     * @return true if readable, false otherwise
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}