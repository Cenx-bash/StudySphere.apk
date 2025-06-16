package com.example.studysphere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Log.e(TAG, "BootReceiver: Context or Intent is null, skipping execution");
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted, rescheduling reminders...");

            // Run scheduling on the main thread safely
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    ReminderScheduler scheduler = new ReminderScheduler(context);
                    scheduler.scheduleReminder();

                    // Also trigger ReminderReceiver to check and notify immediately if needed
                    triggerReminderReceiver(context);

                    Log.d(TAG, "Reminders successfully rescheduled after boot");
                } catch (SecurityException e) {
                    Log.e(TAG, "SecurityException: Missing required permissions - " + e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Error while rescheduling reminders: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Triggers ReminderReceiver manually after boot to send immediate reminders if needed.
     */
    private void triggerReminderReceiver(Context context) {
        Log.d(TAG, "Triggering ReminderReceiver manually after boot");

        Intent reminderIntent = new Intent(context, ReminderReceiver.class);
        reminderIntent.putExtra(ReminderReceiver.EXTRA_TITLE, "Welcome Back!");
        reminderIntent.putExtra(ReminderReceiver.EXTRA_CONTENT, "Your scheduled reminders are now active.");

        context.sendBroadcast(reminderIntent);
    }
}
