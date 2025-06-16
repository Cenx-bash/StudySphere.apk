package com.example.studysphere;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class Profile_User extends AppCompatActivity {
    private EditText editTextName, editTextAge, editTextSchool, editTextEmail, editTextPhone, editTextAddress;
    private Spinner spinnerGender, spinnerStrand;
    private Button btnSave, btnCancel;
    private ImageView profileImageView;
    private DatabaseHelper databaseHelper;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile_user);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        databaseHelper = new DatabaseHelper(this);

        profileImageView = findViewById(R.id.Changepicture);
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerStrand = findViewById(R.id.spinnerStrand);
        editTextSchool = findViewById(R.id.editTextSchool);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        setupSpinners();
        loadUserProfile();

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            saveImageLocally(selectedImageUri);
                        }
                    } else {
                        Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
                    }
                });

        profileImageView.setOnClickListener(v -> checkPermissionsAndOpenGallery());
        btnSave.setOnClickListener(v -> saveUserProfile());
        btnCancel.setOnClickListener(v -> showCancelConfirmation());
    }

    private void setupSpinners() {
        List<String> genderOptions = Arrays.asList("Select Gender", "Male", "Female", "Other");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genderOptions);
        spinnerGender.setAdapter(genderAdapter);

        List<String> strandOptions = Arrays.asList("Select Strand", "STEM", "ABM", "HUMSS", "ICT");
        ArrayAdapter<String> strandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, strandOptions);
        spinnerStrand.setAdapter(strandAdapter);
    }

    private void checkPermissionsAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 101);
            return;
        }
        openGallery();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveImageLocally(Uri selectedImageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(selectedImageUri)) {
            if (inputStream != null) {
                File file = new File(getFilesDir(), "profile_picture.jpg");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                imageUri = Uri.fromFile(file);
                profileImageView.setImageURI(imageUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserProfile() {
        String name = editTextName.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String strand = spinnerStrand.getSelectedItem().toString();
        String school = editTextSchool.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String imagePath = (imageUri != null) ? imageUri.toString() : "";

        boolean success = databaseHelper.saveUserProfile(name, age, gender, strand, school, email, phone, address, imagePath);
        Toast.makeText(this, success ? "Profile saved successfully!" : "Failed to save profile", Toast.LENGTH_SHORT).show();
    }

    private void loadUserProfile() {
        Cursor cursor = databaseHelper.getUserProfile();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    editTextName.setText(cursor.getString(1));
                    editTextAge.setText(cursor.getString(2));
                    setSpinnerValue(spinnerGender, cursor.getString(3));
                    setSpinnerValue(spinnerStrand, cursor.getString(4));
                    editTextSchool.setText(cursor.getString(5));
                    editTextEmail.setText(cursor.getString(6));
                    editTextPhone.setText(cursor.getString(7));
                    editTextAddress.setText(cursor.getString(8));
                    String imageUriString = cursor.getString(9);
                    if (imageUriString != null && !imageUriString.isEmpty()) {
                        imageUri = Uri.parse(imageUriString);
                        profileImageView.setImageURI(imageUri);
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

    private void showCancelConfirmation() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to cancel?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
    }

    public void openMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

}
