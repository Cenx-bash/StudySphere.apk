package com.example.studysphere;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainNote extends AppCompatActivity {

    private static final String TAG = "MainNote";
    private static final int FILE_PICKER_REQUEST_CODE = 100;

    private LinearLayout recentNotesContainer;
    private SharedPreferences sharedPreferences;
    private List<Note> notesList = new ArrayList<>();
    private View profileImageView;
    private BroadcastReceiver profileUpdateReceiver;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_note);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initializeUI();
        loadRecentNotes();
        profileImageView = findViewById(R.id.profileImageView);
        recentNotesContainer = findViewById(R.id.recentNotesLayout);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        sharedPreferences = getSharedPreferences("RecentNotes", MODE_PRIVATE);

        loadNotes();
        loadProfileImage();

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

        // ✅ Register receiver only ONCE
        IntentFilter filter = new IntentFilter("com.example.studysphere.PROFILE_UPDATED");
        registerReceiver(profileUpdateReceiver, filter);
    }

    private void loadNotes() {
        String savedNotes = sharedPreferences.getString("savedNotes", "");

        Log.d(TAG, "📜 Raw Saved Notes: " + savedNotes); // Debugging output

        if (savedNotes == null || savedNotes.isEmpty()) {
            Log.d(TAG, "⚠️ No saved notes found.");
            return;
        }

        notesList.clear();
        String[] notesArray = savedNotes.split("\\|\\|");

        for (String noteEntry : notesArray) {
            String[] noteParts = noteEntry.split(";;");
            if (noteParts.length == 3) {
                String title = noteParts[0].trim();
                String content = noteParts[1].trim();
                String timestamp = noteParts[2].trim();
                notesList.add(new Note(title, content, timestamp));
            }
        }

        Log.d(TAG, "✅ Loaded " + notesList.size() + " notes.");
        displayNotes(); // Call this here
    }

    private void displayNotes() {
        if (recentNotesContainer == null) {
            Log.e(TAG, "❌ recentNotesContainer is NULL. Check XML layout.");
            return;
        }

        recentNotesContainer.removeAllViews(); // Clear existing views before adding new ones

        if (notesList.isEmpty()) {
            Log.d(TAG, "⚠️ No notes to display.");
            return;
        }

        for (Note note : notesList) {
            createMainNoteView(note);
        }

        Log.d(TAG, "✅ Displayed " + notesList.size() + " notes.");
    }

    private void createMainNoteView(Note note) {
        if (note.getTitle().isEmpty()) {
            Log.e(TAG, "❌ Skipping empty note: " + note);
            return;
        }

        // Create note container
        LinearLayout noteContainer = new LinearLayout(this);
        noteContainer.setOrientation(LinearLayout.VERTICAL);
        noteContainer.setPadding(20, 20, 20, 20);
        noteContainer.setBackgroundResource(R.drawable.note_background);
        noteContainer.setElevation(5);

        // Create TextView for title
        TextView titleView = new TextView(this);
        titleView.setText(note.getTitle());
        titleView.setTextSize(18);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextColor(getResources().getColor(R.color.black));

        noteContainer.addView(titleView);

        // Create TextView for content
        TextView contentView = new TextView(this);
        contentView.setText(note.getContent()); // Displays note content
        contentView.setTextSize(16);
        contentView.setTextColor(getResources().getColor(R.color.gray));

        noteContainer.addView(contentView);

        // Set layout params
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(20, 10, 20, 10);
        noteContainer.setLayoutParams(layoutParams);

        recentNotesContainer.addView(noteContainer);
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume() called, reloading notes...");
        loadNotes();
    }

    private void initializeUI() {
        recentNotesContainer = findViewById(R.id.recentNotesLayout);
        sharedPreferences = getSharedPreferences("RecentNotes", MODE_PRIVATE);

        if (recentNotesContainer == null) {
            Log.e(TAG, "❌ recentNotesContainer is NULL. Fix XML layout.");
        } else {
            Log.d(TAG, "✅ recentNotesContainer found.");
        }

        applyWindowInsets();
    }

    private void applyWindowInsets() {
        View noteView = findViewById(R.id.mainNote);
        if (noteView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(noteView, (v, insets) -> {
                v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                        insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                        insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                        insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
                return insets;
            });
        } else {
            Log.e(TAG, "View with ID 'mainNote' not found.");
        }
    }

    private void loadRecentNotes() {
        if (recentNotesContainer == null) {
            Log.e(TAG, "❌ RecentNotesContainer is null.");
            return;
        }

        recentNotesContainer.removeAllViews();
        String recentNotes = sharedPreferences.getString("recentNotes", "");

        // Log the retrieved notes
        Log.d(TAG, "📜 Loaded Notes: " + recentNotes);

        if (!recentNotes.isEmpty()) {
            String[] notesArray = recentNotes.split("\n");

            for (String noteEntry : notesArray) {
                if (!noteEntry.trim().isEmpty()) {
                    TextView textView = new TextView(this);
                    textView.setText(noteEntry);
                    textView.setPadding(10, 5, 10, 5);
                    textView.setTextSize(16);
                    textView.setTextColor(getResources().getColor(R.color.black));
                    recentNotesContainer.addView(textView);
                }
            }
        } else {
            Log.d(TAG, "⚠️ No recent notes found.");
        }
    }

    private void saveRecentNote(String noteTitle, String noteContent) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String existingNotes = sharedPreferences.getString("recentNotes", "");
        String timeStamp = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(new Date());

        // Save in the format: FileName - Timestamp | FileContent
        String newEntry = noteTitle + " - " + timeStamp + " | " + noteContent + "\n";

        existingNotes = newEntry + existingNotes;

        editor.putString("recentNotes", existingNotes);
        editor.apply();

        Log.d(TAG, "Saved recent note: " + newEntry);
    }

    public void openFilePicker(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");  // You can adjust the type to limit file types if needed
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    Log.d(TAG, "Selected file: " + uri.toString());

                    // Get file type
                    String fileType = getContentResolver().getType(uri);
                    if (fileType == null) {
                        Log.d(TAG, "File type could not be determined. Attempting to handle file as plain text.");
                        fileType = "text/plain"; // Fallback to text
                    }

                    // Handle file based on type
                    switch (fileType) {
                        case "application/pdf":
                            String pdfContent = FileUtils.readPdf(this, uri);
                            if (pdfContent != null) displayContent(pdfContent);
                            break;

                        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                            String docxContent = FileUtils.readDocx(this, uri);
                            if (docxContent != null) displayContent(docxContent);
                            break;

                        case "text/plain":
                            String textContent = FileUtils.readTextFile(this, uri);
                            if (textContent != null) displayContent(textContent);
                            break;

                        default:
                            if (fileType.startsWith("image/")) {
                                displayImage(uri);
                            } else {
                                Log.d(TAG, "Unsupported file type: " + fileType);
                            }
                            break;
                    }
                }
            }
        }
    }


    private void displayContent(String content) {
        if (content != null) {
            // You can display the content in a TextView or any other UI element
            TextView contentTextView = new TextView(this);
            contentTextView.setText(content);
            recentNotesContainer.addView(contentTextView);
        } else {
            Log.d(TAG, "Failed to read the file content.");
        }
    }

    private String getFileType(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        return contentResolver.getType(uri);
    }

    // Read the content of a PDF file
    private void readPdfFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                // Implement PDF reading logic here
                // For simplicity, let's just log the file
                Log.d(TAG, "PDF file selected: " + uri.toString());
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading PDF file: ", e);
        }
    }

    // Read the content of a Word (DOCX) file
    private void readWordFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                // Implement Word reading logic here
                // For simplicity, let's just log the file
                Log.d(TAG, "Word file selected: " + uri.toString());
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading Word file: ", e);
        }
    }

    // Read the content of a text file
    private void readTextFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                saveRecentNote(getFileName(uri), content.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading text file: ", e);
        }
    }

    // Display image files (JPG, PNG)
    private void displayImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            recentNotesContainer.addView(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error displaying image: ", e);
        }
    }

    // Get the file name from the URI
    private String getFileName(Uri uri) {
        String result = null;
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index != -1) {
                    result = cursor.getString(index);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name", e);
        }
        return result != null ? result : "Unknown File";
    }

    public void openMainActivity(View view) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(new Intent(this, MainActivity.class));
    }

    public void openScheduleActivity(View view) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(new Intent(this, Schedule.class));
    }

    public void openProfile_User(View view) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(new Intent(this, Profile_User.class));
    }

    public void openNotepadActivity(View view) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(new Intent(this, Notepad.class));
    }
}