package com.example.studysphere;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";
    private static final int REQUEST_CODE = 1001;
    private static final long INTERVAL_3_HOURS = 3 * 60 * 60 * 1000L;

    private final Context context;
    private final AlarmManager alarmManager;

    public ReminderScheduler(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
    }

    public void scheduleReminder() {
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is not available");
            return;
        }

        PendingIntent pendingIntent = getReminderPendingIntent(PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            Log.d(TAG, "Reminder already scheduled, skipping new schedule");
            return;
        }

        pendingIntent = getReminderPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.HOUR, 3); // First trigger after 3 hours

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                INTERVAL_3_HOURS,
                pendingIntent
        );

        Log.d(TAG, "Reminder scheduled to repeat every 3 hours");
    }

    public void cancelReminder() {
        PendingIntent pendingIntent = getReminderPendingIntent(PendingIntent.FLAG_NO_CREATE);
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Reminder canceled successfully");
        } else {
            Log.e(TAG, "No reminder found to cancel");
        }
    }

    /**
     * Creates or retrieves a PendingIntent for the reminder.
     *
     * @param flag The PendingIntent flag (FLAG_UPDATE_CURRENT, FLAG_NO_CREATE, etc.)
     * @return The PendingIntent or null if FLAG_NO_CREATE is used and no intent exists.
     */
    private PendingIntent getReminderPendingIntent(int flag) {
        Intent intent = new Intent(context, ReminderReceiver.class); // Ensure ReminderReceiver is defined
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                flag | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
