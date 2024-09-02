package es.rcti.demoprinterplus.sistemamulta2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "User.db";
    public static final String TABLE_NAME = "user_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "USERNAME";
    public static final String COL_3 = "PASSWORD";
    public static final String COL_4 = "NOMBRE";
    public static final String COL_5 = "APELLIDO";
    public static final String COL_6 = "LEGAJO";
    public static final String COL_7 = "PHOTO";  // New column for storing photos

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, PASSWORD TEXT, NOMBRE TEXT, APELLIDO TEXT, LEGAJO TEXT, PHOTO BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String username, String password, String nombre, String apellido, String legajo, @Nullable byte[] photo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, username);
        contentValues.put(COL_3, password);
        contentValues.put(COL_4, nombre);
        contentValues.put(COL_5, apellido);
        contentValues.put(COL_6, legajo);

        if (photo != null) {
            contentValues.put(COL_7, photo);  // Si se proporciona una foto, se inserta
        }

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE USERNAME = ? AND PASSWORD = ?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public String getFullName(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT NOMBRE, APELLIDO FROM " + TABLE_NAME + " WHERE USERNAME = ?", new String[]{username});
        String fullName = "";
        if (cursor.moveToFirst()) {
            fullName = cursor.getString(0) + " " + cursor.getString(1);
        }
        cursor.close();
        return fullName;
    }
}
