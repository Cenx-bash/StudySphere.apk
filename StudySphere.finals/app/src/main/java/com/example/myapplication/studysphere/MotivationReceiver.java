package com.example.studysphere;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Random;

public class MotivationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "MOTIVATION_CHANNEL";
    private static final int NOTIFICATION_ID = 101;

    @Override
    public void onReceive(Context context, Intent intent) {
        String[] motivationalQuotes = {
                "Believe in yourself and all that you are.",
                "The secret of getting ahead is getting started.",
                "Your only limit is your mind.",
                "Do something today that your future self will thank you for.",
                "Success is not final, failure is not fatal: it is the courage to continue that counts.",
                "Dream big and dare to fail.",
                "Great things never come from comfort zones.",
                "Push yourself, because no one else is going to do it for you.",
                "Don’t stop when you’re tired. Stop when you’re done.",
                "Little by little, one travels far.",
                "Hard work beats talent when talent doesn’t work hard.",
                "Your future is created by what you do today, not tomorrow.",
                "You don’t have to be great to start, but you have to start to be great.",
                "Discipline is the bridge between goals and accomplishment.",
                "Doubt kills more dreams than failure ever will.",
                "You are capable of amazing things.",
                "The way to get started is to quit talking and begin doing.",
                "Everything you’ve ever wanted is on the other side of fear.",
                "You are stronger than you think.",
                "Difficult roads often lead to beautiful destinations.",
                "Believe in your infinite potential. Your only limitations are those you set upon yourself.",
                "Don’t wait for opportunity. Create it.",
                "It’s going to be hard, but hard does not mean impossible."
        };

        int randomIndex = new Random().nextInt(motivationalQuotes.length);
        String quote = motivationalQuotes[randomIndex];

        sendNotification(context, quote);
    }

    private void sendNotification(Context context, String quote) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return; // Avoid NullPointerException
        }

        // Create notification channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Motivation Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Daily motivation to keep you going!"); // Added a description
            notificationManager.createNotificationChannel(channel);
        }

        // Open the app when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Fixes Android 12+ compatibility
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.studyshpere)
                .setContentTitle("Stay Motivated! 🚀")
                .setContentText(quote)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
