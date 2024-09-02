package es.rcti.demoprinterplus.sistemamulta2.photos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class TrafficViolations extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TrafficViolations.db";
    public static final String TABLE_NAME = "violations_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "USERNAME";
    public static final String COL_3 = "PHOTO";
    public static final String COL_4 = "APELLIDO_NOMBRE";
    public static final String COL_5 = "DOMICILIO";
    public static final String COL_6 = "LOCALIDAD";
    public static final String COL_7 = "TIPO_DOCUMENTO";
    public static final String COL_8 = "NUMERO_DOCUMENTO";
    public static final String COL_9 = "DOMINIO";
    public static final String COL_10 = "MARCA";
    public static final String COL_11 = "MODELO";
    public static final String COL_12 = "TIPO_VEHICULO";
    public static final String COL_13 = "INFRACCION";
    public static final String COL_14 = "LUGAR";
    public static final String COL_15 = "FECHA_HORA";

    public TrafficViolations(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_2 + " TEXT, " +
                COL_3 + " BLOB, " +
                COL_4 + " TEXT, " +
                COL_5 + " TEXT, " +
                COL_6 + " TEXT, " +
                COL_7 + " TEXT, " +
                COL_8 + " TEXT, " +
                COL_9 + " TEXT, " +
                COL_10 + " TEXT, " +
                COL_11 + " TEXT, " +
                COL_12 + " TEXT, " +
                COL_13 + " TEXT, " +
                COL_14 + " TEXT, " +
                COL_15 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertViolation(String username, @Nullable byte[] photo, String apellidoNombre,
                                   String domicilio, String localidad, String tipoDocumento,
                                   String numeroDocumento, String dominio, String marca,
                                   String modelo, String tipoVehiculo, String infraccion,
                                   String lugar, String fechaHora) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, username);
        if (photo != null) {
            contentValues.put(COL_3, photo);
        }
        contentValues.put(COL_4, apellidoNombre);
        contentValues.put(COL_5, domicilio);
        contentValues.put(COL_6, localidad);
        contentValues.put(COL_7, tipoDocumento);
        contentValues.put(COL_8, numeroDocumento);
        contentValues.put(COL_9, dominio);
        contentValues.put(COL_10, marca);
        contentValues.put(COL_11, modelo);
        contentValues.put(COL_12, tipoVehiculo);
        contentValues.put(COL_13, infraccion);
        contentValues.put(COL_14, lugar);
        contentValues.put(COL_15, fechaHora);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }
}