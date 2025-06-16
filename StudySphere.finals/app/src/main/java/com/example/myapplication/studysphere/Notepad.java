package com.example.studysphere;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Notepad extends AppCompatActivity {

    private final List<Note> notesList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private LinearLayout notesContainer;
    private DrawerLayout drawerLayout;
    private EditText titleEditText, contentEditText;
    private Button saveButton, backButton;
    private ScrollView notesScrollView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_note_pad);

        // Initialize UI elements
        drawerLayout = findViewById(R.id.drawerLayout);
        notesContainer = findViewById(R.id.notesContainer);
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        notesScrollView = findViewById(R.id.notesScrollView);

        sharedPreferences = getSharedPreferences("RecentNotes", MODE_PRIVATE);

        // Set listeners
        saveButton.setOnClickListener(v -> saveNote());
        backButton.setOnClickListener(v -> openMainNote());


        // Load saved notes
        loadNotes();
    }


    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (!title.isEmpty() && !content.isEmpty()) {
            // Generate timestamp
            String currentTimestamp = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(new Date());

            // Store only title without timestamp
            notesList.add(new Note(title, content, currentTimestamp));

            saveNotesToStorage();
            displayNotes();
            clearInputFields();
            showToast("Note saved successfully.");
        } else {
            if (title.isEmpty()) titleEditText.setError("Title is required");
            if (content.isEmpty()) contentEditText.setError("Content is required");
        }
    }


    private void saveNotesToStorage() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        StringBuilder savedNotes = new StringBuilder();

        for (Note note : notesList) {
            savedNotes.append(note.getTitle()).append(";;")  // Store title without timestamp
                    .append(note.getContent()).append(";;")
                    .append(note.getTimestamp()).append("||");  // Store timestamp separately
        }

        editor.putString("savedNotes", savedNotes.toString());
        editor.apply();

        Log.d(TAG, "💾 Notes saved successfully: " + savedNotes);
    }
    private void loadNotes() {
        String savedNotes = sharedPreferences.getString("savedNotes", "");

        if (savedNotes == null || savedNotes.isEmpty()) {
            return; // No saved notes, exit early
        }

        notesList.clear(); // Clear existing notes only if new ones are available

        try {
            String[] notesArray = savedNotes.split("\\|\\|"); // Escape '||' properly
            for (String noteEntry : notesArray) {
                String[] noteParts = noteEntry.split(";;");
                if (noteParts.length == 3) {
                    String title = noteParts[0].trim();   // Title without timestamp
                    String content = noteParts[1].trim();
                    String timestamp = noteParts[2].trim(); // Separate timestamp

                    if (!title.isEmpty() && !content.isEmpty() && !timestamp.isEmpty()) {
                        notesList.add(new Note(title, content, timestamp));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error loading notes.");
        }

        displayNotes();
    }



    private void displayNotes() {
        notesContainer.removeAllViews();
        for (int i = 0; i < notesList.size(); i++) {
            createNoteView(notesList.get(i), i);
        }
        notesScrollView.post(() -> notesScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void createNoteView(Note note, int index) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View noteView = inflater.inflate(R.layout.note_item, notesContainer, false);

        // Set title and content
        ((TextView) noteView.findViewById(R.id.titleTextView)).setText(note.getTitle());
        ((TextView) noteView.findViewById(R.id.contentTextView)).setText(note.getContent());
        ((TextView) noteView.findViewById(R.id.timestampTextView)).setText(note.getTimestamp());


        // Apply margin (10dp top margin)
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 10, 0, 0); // (left, top, right, bottom) in pixels
        noteView.setLayoutParams(layoutParams);

        noteView.setOnLongClickListener(v -> {
            showPopupMenu(v, index);
            return true;
        });

        notesContainer.addView(noteView);
    }



    private void showPopupMenu(View view, int index) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.note_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.edit_note) {
                editNote(index);
                return true;
            } else if (item.getItemId() == R.id.delete_note) {
                deleteNote(index);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void editNote(int index) {
        Note note = notesList.get(index);
        titleEditText.setText(note.getTitle());
        contentEditText.setText(note.getContent());
    }

    private void deleteNote(int index) {
        notesList.remove(index);
        saveNotesToStorage();
        displayNotes();
        showToast("Note deleted successfully.");
    }

    private void clearInputFields() {
        titleEditText.getText().clear();
        contentEditText.getText().clear();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void openMainNote() {
        Intent intent = new Intent(this, MainNote.class);
        intent.putExtra("savedNotes", sharedPreferences.getString("savedNotes", ""));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
