package com.example.studysphere;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "outlook_notifications";
    private static final String CHANNEL_NAME = "Outlook Notifications";
    private static final int NOTIFICATION_ID = 1001;

    private static NotificationHelper instance;
    private final Context context;
    private final NotificationManager notificationManager;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private NotificationHelper(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    /**
     * Returns the singleton instance of NotificationHelper.
     */
    public static synchronized NotificationHelper getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new NotificationHelper(context);
        }
        return instance;
    }

    /**
     * Sends a notification with the specified title and message.
     */
    public void showNotification(@NonNull String title, @NonNull String message) {
        if (notificationManager == null) {
            Log.e(TAG, "NotificationManager is null, cannot send notification");
            return;
        }

        Notification notification = buildNotification(title, message);
        notificationManager.notify(NOTIFICATION_ID, notification);
        Log.d(TAG, "Notification sent: " + title);
    }

    /**
     * Creates the notification channel for Android 8.0+.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Channel for Outlook notifications");
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 500, 250, 500});

                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created: " + CHANNEL_NAME);
            } else {
                Log.d(TAG, "Notification channel already exists, skipping creation");
            }
        }
    }

    /**
     * Builds the notification with the specified title and message.
     */
    private Notification buildNotification(@NonNull String title, @NonNull String message) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.studyshpere)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setLights(Color.BLUE, 1000, 1000)
                .build();
    }
}
