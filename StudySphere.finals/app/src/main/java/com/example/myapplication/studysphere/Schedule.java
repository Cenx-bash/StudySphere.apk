package com.example.studysphere;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Schedule extends AppCompatActivity {

    private static final String TAG = "ScheduleActivity";
    private Button setAlarmButton;
    private TextView addedSchedule;
    private CalendarView calendarView;
    private EditText alarmLabelInput;
    private int selectedYear = -1, selectedMonth = -1, selectedDay = -1;
    private Object timestamp;
    private View profileImageView;
    private BroadcastReceiver profileUpdateReceiver;

    @SuppressLint({"MissingInflatedId", "UnspecifiedRegisterReceiverFlag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_schedule);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        setAlarmButton = findViewById(R.id.setAlarmButton);
        addedSchedule = findViewById(R.id.addedSchedule);
        calendarView = findViewById(R.id.calendarView);
        alarmLabelInput = findViewById(R.id.alarmLabelInput);
        profileImageView = findViewById(R.id.profileImageView);


        addedSchedule.setVisibility(View.GONE); 

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = dayOfMonth;
            String formattedDate = formatDate(year, month, dayOfMonth);
            updateScheduleInfo(formattedDate);
            Log.d(TAG, "Selected Date: " + formattedDate);
        });
        
        loadProfileImage();

        // Set Alarm button click event
        setAlarmButton.setOnClickListener(v -> {
            if (selectedYear == -1) {
                Toast.makeText(this, "Please select a date first!", Toast.LENGTH_SHORT).show();
            } else {
                showTimePickerDialog();
            }
        });
        profileUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String imagePath = intent.getStringExtra("imagePath");
                if (imagePath != null && !imagePath.isEmpty()) {
                    Glide.with(context)
                            .load(Uri.parse(imagePath))
                            .into((ImageView) profileImageView);
                }
            }
        };

        IntentFilter filter = new IntentFilter("com.example.studysphere.PROFILE_UPDATED");
        registerReceiver(profileUpdateReceiver, filter);
    }


    private void loadProfileImage() {
        Cursor cursor = new DatabaseHelper(this).getUserProfile();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String imageUriString = cursor.getString(9);
                    if (imageUriString != null && !imageUriString.isEmpty()) {
                        Glide.with(this)
                                .load(Uri.parse(imageUriString))
                                .into((ImageView) profileImageView);
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileUpdateReceiver != null) {
            unregisterReceiver(profileUpdateReceiver);
        }
    }

    private void updateScheduleInfo(String date) {
        addedSchedule.setVisibility(View.VISIBLE);
        addedSchedule.setText("Selected date: " + date);
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
            setSchedule(hourOfDay, minute);
        }, currentHour, currentMinute, false).show();
    }

    private void setSchedule(int hour, int minute) {
        String alarmLabel = alarmLabelInput.getText().toString().trim();
        if (alarmLabel.isEmpty()) alarmLabel = "Reminder";

        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth, selectedDay, hour, minute, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "Cannot set alarm for a past time!", Toast.LENGTH_SHORT).show();
            return;
        }

        saveAlarmToSharedPreferences(calendar.getTimeInMillis(), alarmLabel);
        setAlarm(calendar.getTimeInMillis(), alarmLabel);
    }

    private void saveAlarmToSharedPreferences(long alarmTime, String label) {
        SharedPreferences prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        Set<String> alarms = prefs.getStringSet("scheduledAlarms", new HashSet<>());

        Set<String> updatedAlarms = new HashSet<>(alarms); // Make a mutable copy

        String formattedDate = formatDate(alarmTime);
        String formattedAlarm = label + "|" + alarmTime + "|" + formattedDate; // Corrected format

        updatedAlarms.add(formattedAlarm);
        prefs.edit().putStringSet("scheduledAlarms", updatedAlarms).apply();

        Log.d(TAG, "Saved alarm: " + formattedAlarm);

    }

    private String formatDate(long alarmTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date(alarmTime));
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(long alarmTime, String label) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("ALARM_LABEL", label);

        int requestCode = (int) (alarmTime % Integer.MAX_VALUE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            Toast.makeText(this, "Alarm set successfully!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Alarm set for: " + alarmTime);
        } else {
            Toast.makeText(this, "Failed to set alarm!", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }

    public void openMainActivity(View view) {
        navigateToActivity(MainActivity.class);
    }

    public void openMainNoteActivity(View view) {
        navigateToActivity(MainNote.class);
    }

    public void openProfile_User(View view) {
        navigateToActivity(Profile_User.class);
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
