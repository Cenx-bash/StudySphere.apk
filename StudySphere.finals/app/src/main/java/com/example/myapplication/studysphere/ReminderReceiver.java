package com.example.studysphere;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Random;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "reminder_channel";
    private static final String TAG = "ReminderReceiver";
    private static MediaPlayer mediaPlayer;

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_CONTENT = "extra_content";
    private int alarmRingCount = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Log.e(TAG, "ReminderReceiver: Context or Intent is null");
            return;
        }

        Log.d(TAG, "ReminderReceiver triggered successfully");

        String title = intent.getStringExtra(EXTRA_TITLE);
        String content = intent.getStringExtra(EXTRA_CONTENT);
        String alarmLabel = intent.getStringExtra("ALARM_LABEL");

        if ((title == null || title.trim().isEmpty()) && (alarmLabel == null || alarmLabel.trim().isEmpty())) {
            String[] titles = {"Study Reminder!", "Time to Review!", "Don't Forget!", "Heads Up!", "Keep Studying!", "Stay Focused!", "Review Time!", "Assignment Reminder!", "Task Alert!", "Stay on Track!", "Push Yourself!", "Learning Time!"};
            String[] contents = {
                    "Don't forget about your ELMS due!",
                    "Make time for some study!",
                    "Check your tasks for today!",
                    "Keep up with your study plan!",
                    "Stay ahead of deadlines!",
                    "Revise your notes today!",
                    "Prepare for upcoming exams!",
                    "Complete your assignments on time!",
                    "Plan your study schedule wisely!",
                    "Stay consistent with learning!",
                    "Small progress is still progress!",
                    "Sharpen your skills with practice!"
            };

            Random random = new Random();
            title = titles[random.nextInt(titles.length)];
            content = contents[random.nextInt(contents.length)];
        } else if (alarmLabel != null) {
            title = "Scheduled Alarm";
            content = alarmLabel;
        }

        Log.d(TAG, "Sending Notification -> Title: " + title + ", Content: " + content);

        // Show notification
        showNotification(context, title, content);

        // Play alarm sound once or twice
        playAlarmSound(context);

        // Vibrate phone once or twice
        vibratePhone(context);
    }

    private void showNotification(Context context, String title, String content) {
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.studyshpere)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        } else {
            Log.e(TAG, "NotificationManager is null. Notification not sent.");
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reminder Channel";
            String description = "Channel for alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.e(TAG, "Failed to create notification channel: NotificationManager is null.");
            }
        }
    }

    private void playAlarmSound(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.videoplayback);
            if (mediaPlayer == null) {
                Log.e(TAG, "Failed to create MediaPlayer instance.");
                return;
            }
        }

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.setLooping(false);
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                alarmRingCount++;
                if (alarmRingCount < 1) {
                    // Play the alarm again after it finishes
                    mp.start();
                } else {
                    stopAlarmSound(); // Stop after the second ring
                }
            });
        }
    }

    // Stop the alarm sound
    public void stopAlarmSound() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            alarmRingCount = 0; // Reset the count after stopping the alarm
        }
    }

    private void vibratePhone(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            long[] pattern = {0, 1000, 500, 1000};  // Vibrate for 1 second, pause for 0.5 second, repeat
            vibrator.vibrate(pattern, -1);  // -1 means the vibration will not repeat
        } else {
            Log.e(TAG, "Vibrator service is null. Unable to vibrate.");
        }
    }
}
