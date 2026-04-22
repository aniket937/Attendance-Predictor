package com.example.attendance.utils;

/**
 * Utility class for attendance calculations and predictions.
 * Provides methods to calculate attendance percentage and predict future attendance.
 */
public class AttendanceUtils {

    public static final double LOW_THRESHOLD = 75.0;
    public static final double CAREFUL_THRESHOLD = 80.0;

    public static final String STATUS_LOW = "Low Attendance";
    public static final String STATUS_CAREFUL = "Be Careful";
    public static final String STATUS_SAFE = "Safe Zone";

    /**
     * Calculate attendance percentage
     * @param attended Number of classes attended
     * @param total Total number of classes
     * @return Attendance percentage (0-100), or 0 if total is 0
     */
    public static double calculateAttendance(int attended, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return (double) attended / total * 100;
    }

    /**
     * Calculate attendance percentage if user attends next class
     * @param attended Current classes attended
     * @param total Current total classes
     * @return New attendance percentage after attending
     */
    public static double attendNext(int attended, int total) {
        if (total < 0) {
            return 0.0;
        }
        return (double) (attended + 1) / (total + 1) * 100;
    }

    /**
     * Calculate attendance percentage if user skips next class
     * @param attended Current classes attended
     * @param total Current total classes
     * @return New attendance percentage after skipping
     */
    public static double skipNext(int attended, int total) {
        if (total < 0) {
            return 0.0;
        }
        return (double) attended / (total + 1) * 100;
    }

    /**
     * Calculate attendance percentage if user skips multiple classes
     * @param attended Current classes attended
     * @param total Current total classes
     * @param skipCount Number of classes to skip
     * @return New attendance percentage after skipping n classes
     */
    public static double skipMultiple(int attended, int total, int skipCount) {
        if (total < 0 || skipCount < 0) {
            return 0.0;
        }
        return (double) attended / (total + skipCount) * 100;
    }

    /**
     * Calculate maximum number of classes that can be skipped while maintaining
     * attendance above the given threshold
     * @param attended Current classes attended
     * @param total Current total classes
     * @param threshold Minimum attendance percentage to maintain
     * @return Maximum number of classes that can be skipped
     */
    public static int maxBunks(int attended, int total, double threshold) {
        if (total <= 0 || attended < 0 || threshold <= 0) {
            return 0;
        }

        double currentAttendance = calculateAttendance(attended, total);
        if (currentAttendance < threshold) {
            return 0;
        }

        int maxBunks = 0;
        for (int i = 0; i <= 1000; i++) {
            double projectedAttendance = skipMultiple(attended, total, i);
            if (projectedAttendance >= threshold) {
                maxBunks = i;
            } else {
                break;
            }
        }
        return maxBunks;
    }

    /**
     * Get attendance status based on percentage
     * @param percentage Attendance percentage
     * @return Status string: "Low Attendance", "Be Careful", or "Safe Zone"
     */
    public static String getStatus(double percentage) {
        if (percentage < LOW_THRESHOLD) {
            return STATUS_LOW;
        } else if (percentage < CAREFUL_THRESHOLD) {
            return STATUS_CAREFUL;
        } else {
            return STATUS_SAFE;
        }
    }

    /**
     * Get status color resource ID based on attendance percentage
     * @param percentage Attendance percentage
     * @return Color resource ID
     */
    public static int getStatusColor(double percentage) {
        if (percentage < LOW_THRESHOLD) {
            return android.graphics.Color.parseColor("#F44336"); // Red
        } else if (percentage < CAREFUL_THRESHOLD) {
            return android.graphics.Color.parseColor("#FFC107"); // Yellow
        } else {
            return android.graphics.Color.parseColor("#4CAF50"); // Green
        }
    }

    /**
     * Format percentage for display
     * @param percentage Percentage value
     * @return Formatted string with 2 decimal places
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.2f%%", percentage);
    }

    /**
     * Validate input for subject
     * @param name Subject name
     * @param total Total classes
     * @param attended Classes attended
     * @return Error message or null if valid
     */
    public static String validateInput(String name, String total, String attended) {
        if (name == null || name.trim().isEmpty()) {
            return "Please enter subject name";
        }

        if (total == null || total.trim().isEmpty()) {
            return "Please enter total classes";
        }

        if (attended == null || attended.trim().isEmpty()) {
            return "Please enter classes attended";
        }

        try {
            int totalClasses = Integer.parseInt(total);
            int attendedClasses = Integer.parseInt(attended);

            if (totalClasses < 0) {
                return "Total classes cannot be negative";
            }

            if (attendedClasses < 0) {
                return "Classes attended cannot be negative";
            }

            if (attendedClasses > totalClasses) {
                return "Classes attended cannot exceed total classes";
            }

        } catch (NumberFormatException e) {
            return "Please enter valid numbers";
        }

        return null;
    }
}