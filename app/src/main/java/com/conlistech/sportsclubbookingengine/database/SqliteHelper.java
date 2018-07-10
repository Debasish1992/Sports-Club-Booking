package com.conlistech.sportsclubbookingengine.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class SqliteHelper extends SQLiteOpenHelper {

    public SqliteHelper(Context context) {
        super(context, DatabaseConstants.DATABASE_NAME,
                null, DatabaseConstants.DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create notes table
        db.execSQL(DatabaseConstants.CREATE_SPORT_TABLE);
        db.execSQL(DatabaseConstants.CREATE_TEAMMATE_TABLE);
        db.execSQL(DatabaseConstants.CREATE_PAYMENT_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.SPORTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.TEAMMATE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.PAYMENT_TABLE);
        // Create tables again
        onCreate(db);
    }


    // Inserting the sports
    public long insertSports(String sport_id, String sport_name) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(DatabaseConstants.SPORT_ID, sport_id);
        values.put(DatabaseConstants.SPORT_NAME, sport_name);

        // insert row
        long id = db.insert(DatabaseConstants.SPORTS_TABLE, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    // Inserting the sports
    public long insertTeammates(String teammate_id, String teammate_name) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(DatabaseConstants.TEAMMATE_ID, teammate_id);
        values.put(DatabaseConstants.TEAMMATE_NAME, teammate_name);

        // insert row
        long id = db.insert(DatabaseConstants.TEAMMATE_TABLE, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }


    // Inserting Payment Card Details into Sqlite Db
    public long insertPaymentDetails(int card_id,
                                     String card_number,
                                     String card_type,
                                     String card_expiry,
                                     int primary_status) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(DatabaseConstants.CARD_ID, card_id);
        values.put(DatabaseConstants.CARD_NUMBER, card_number);
        values.put(DatabaseConstants.CARD_EXPIRY, card_expiry);
        values.put(DatabaseConstants.CARD_TYPE, card_type);
        values.put(DatabaseConstants.PRIMARY_STATUS, primary_status);


        // insert row
        long id = db.insert(DatabaseConstants.PAYMENT_TABLE, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    // Getting card no from payment table
    public String getPrimaryCardNo(){
        String primaryCardQuery = "SELECT  card_number FROM " +
                DatabaseConstants.PAYMENT_TABLE + " WHERE is_primary = 1";
        String cardNo = null;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(primaryCardQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                cardNo = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CARD_NUMBER));
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();
        return cardNo;
    }



    // Getting the row Count
    public int getSportsCount() {
        String countQuery = "SELECT  * FROM " +
                DatabaseConstants.SPORTS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public int getPaymentCardCount() {
        String countQuery = "SELECT  * FROM " +
                DatabaseConstants.PAYMENT_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public ArrayList<String> getAllSports() {
        // Select All Query
        ArrayList<String> sportsArray = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DatabaseConstants.SPORTS_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                //sportsArray.add(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SPORT_ID)));
                sportsArray.add(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SPORT_NAME)));
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return sportsArray;
    }


    // Getting all friends Ids
    public ArrayList<String> getAllTeammateIds() {
        // Select All Query
        ArrayList<String> teammateArray = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DatabaseConstants.TEAMMATE_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                teammateArray.add(cursor.getString(cursor.getColumnIndex(DatabaseConstants.TEAMMATE_ID)));
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return teammateArray;
    }


    // Getting sport Id
    public String getSportId(String sportName) {
        String sport_id = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c1 = db.rawQuery(
                "SELECT sport_id FROM "+ DatabaseConstants.SPORTS_TABLE + " where sport_name = '"
                        + sportName + "'", null);
        c1.moveToFirst();
        if (c1.getCount() != 0) {
            sport_id = (c1.getString(c1.getColumnIndex("sport_id")));
            c1.moveToNext();
        }
        return sport_id;
    }

    //Clearing table
    public void clearDb(){
        SQLiteDatabase db = getWritableDatabase(); // helper is object extends SQLiteOpenHelper
        //db.delete(DatabaseConstants.SPORTS_TABLE, null, null);
        db.delete(DatabaseConstants.TEAMMATE_TABLE, null, null);
        db.close();
    }

    // Removing all the data from the tables
    public void removeAllRecords(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "+ DatabaseConstants.TEAMMATE_TABLE);
        db.execSQL("delete from "+ DatabaseConstants.PAYMENT_TABLE);
        db.close();
    }
}
