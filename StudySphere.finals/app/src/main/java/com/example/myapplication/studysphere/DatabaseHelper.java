package com.example.studysphere;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserProfile.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "user_profile";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_GENDER = "gender";
    private static final String COLUMN_STRAND = "strand";
    private static final String COLUMN_SCHOOL = "school";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_IMAGE_PATH = "image_path";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_AGE + " TEXT, " +
                COLUMN_GENDER + " TEXT, " +
                COLUMN_STRAND + " TEXT, " +
                COLUMN_SCHOOL + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_ADDRESS + " TEXT, " +
                COLUMN_IMAGE_PATH + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean saveUserProfile(String name, String age, String gender, String strand,
                                   String school, String email, String phone, String address, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, name);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_STRAND, strand);
        values.put(COLUMN_SCHOOL, school);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_IMAGE_PATH, imagePath);

        // Check if user already exists
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " LIMIT 1", null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();

        long result;
        if (exists) {
            // Update existing record
            result = db.update(TABLE_NAME, values, COLUMN_ID + " = (SELECT MIN(" + COLUMN_ID + ") FROM " + TABLE_NAME + ")", null);
        } else {
            // Insert new record
            result = db.insert(TABLE_NAME, null, values);
        }

        db.close();
        return result != -1;
    }

    public Cursor getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1", null);
        return cursor;
    }
}
