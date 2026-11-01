package com.example.planta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "planta.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MY_PLANTS = "my_plants";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_DATE_ADDED = "date_added";

    // Fixed constructor to pass all required parameters to parent SQLiteOpenHelper
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MY_PLANTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_IMAGE_PATH + " TEXT, " +
                COLUMN_DATE_ADDED + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MY_PLANTS);
        onCreate(db);
    }

    // Insert plant item
    public boolean addPlant(String name, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_IMAGE_PATH, imagePath);
        cv.put(COLUMN_DATE_ADDED, currentDate);

        long result = db.insert(TABLE_MY_PLANTS, null, cv);
        return result != -1;
    }

    // Delete plant item
    public void deletePlant(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MY_PLANTS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Fetch all plants model data mapping helper
    public ArrayList<PlantItem> getAllPlants() {
        ArrayList<PlantItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MY_PLANTS + " ORDER BY " + COLUMN_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_ADDED));

                list.add(new PlantItem(id, name, imagePath, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}