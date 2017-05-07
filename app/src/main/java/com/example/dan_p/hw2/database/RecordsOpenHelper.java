package com.example.dan_p.hw2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class RecordsOpenHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;

    // DB name
    private static final String DATABASE_NAME = "records_db";

    // Records table
    private static final String RECORDS_TABLE_NAME = "records";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_DIFFICULTY = "difficulty";
    private static final String COLUMN_LATITUDE= "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_ROTATION = "rotation";

    public RecordsOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + RECORDS_TABLE_NAME);

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + RECORDS_TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_TIME + " INTEGER, "
                + COLUMN_DIFFICULTY + " TEXT, "
                + COLUMN_LATITUDE + " TEXT, "
                + COLUMN_LONGITUDE + " TEXT, "
                + COLUMN_ROTATION + " TEXT " + " );";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RECORDS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertRecord(PlayerRecord record) {

        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, record.getName());
        contentValues.put(COLUMN_TIME, record.getTime());
        contentValues.put(COLUMN_DIFFICULTY, record.getDifficulty());
        contentValues.put(COLUMN_LATITUDE, record.getLatitude());
        contentValues.put(COLUMN_LONGITUDE, record.getLongitude());
        contentValues.put(COLUMN_ROTATION, record.getRotation());
        if (db.insert(RECORDS_TABLE_NAME, null, contentValues) != -1)
            return true;

        return false;
    }

    public ArrayList<PlayerRecord> getTopRecords(String difficulty, int numberOfRecords) {
        ArrayList<PlayerRecord> playerRecords = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT *" +
                                 " FROM " + RECORDS_TABLE_NAME +
                                 " WHERE "+ COLUMN_DIFFICULTY+ " LIKE '" + difficulty +"'"+
                                 " ORDER BY " + COLUMN_TIME + " LIMIT " + numberOfRecords, null);
        if (cursor.moveToFirst()) {
            do {
                PlayerRecord rec = new PlayerRecord();
                rec.setID(cursor.getString(0));
                rec.setName(cursor.getString(1));
                rec.setTime(cursor.getInt(2));
                rec.setDifficulty(cursor.getString(3));
                rec.setLatitude(Double.parseDouble(cursor.getString(4)));
                rec.setLongitude(Double.parseDouble(cursor.getString(5)));
                rec.setRotation(Double.parseDouble(cursor.getString(6)));
                playerRecords.add(rec);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return playerRecords;
    }

    public int getNumberOfRecords(String difficulty) {
        String countQuery = "SELECT * FROM " + RECORDS_TABLE_NAME +
                            " WHERE "+ COLUMN_DIFFICULTY+ " LIKE '" + difficulty +"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count =  cursor.getCount();
        cursor.close();

        return count;
    }

    public int getLowestRecordInTheTable(String difficulty) {
        int max = -1;

        String countQuery = "SELECT MAX("+COLUMN_TIME+") FROM " + RECORDS_TABLE_NAME +
                " WHERE "+ COLUMN_DIFFICULTY+ " LIKE '" + difficulty +"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            max = cursor.getInt(0);
        }
            cursor.close();
        return max;
    }
}
