package com.example.attendance.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.attendance.R;
import com.example.attendance.activities.MainActivity;

/**
 * Helper class for creating and managing notifications.
 * Handles notification channels for Android 8+ and below.
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "attendance_alerts";
    private static final String CHANNEL_NAME = "Attendance Alerts";
    private static final String CHANNEL_DESCRIPTION = "Notifications for low attendance warnings";

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    /**
     * Create notification channel for Android 8+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Show low attendance notification
     * @param subjectName Name of the subject
     * @param percentage Current attendance percentage
     */
    public void showLowAttendanceNotification(String subjectName, double percentage) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        String title = "Low Attendance Alert!";
        String message = String.format("%s: %.2f%% attendance", subjectName, percentage);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(subjectName.hashCode(), builder.build());
        }
    }

    /**
     * Cancel notification for a specific subject
     * @param subjectName Name of the subject
     */
    public void cancelNotification(String subjectName) {
        if (notificationManager != null) {
            notificationManager.cancel(subjectName.hashCode());
        }
    }

    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }
}