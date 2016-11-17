package com.applications.philipp.apkinson.database;

/**
 * Created by Philipp on 09.07.2016.
 */

import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
        import java.util.List;
        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ApkinsonSQLiteHelper extends SQLiteOpenHelper {

    // database version
    private static final int database_VERSION = 2;
    // database name
    private static final String database_NAME = "CallDatabase";
    private static final String callTable = "calls";
    private static final String call_ID = "id";
    private static final String call_DATE = "date";
    private static final String call_DURATION = "duration";
    private static final String call_CONTACTS = "contacts";
    private static final String call_OUTGOING = "outgoing";
    private static final String call_RECORD = "record";
    private static final String call_RESULT = "result";

    private static final String[] COLUMNS = { call_ID, call_DATE, call_DURATION, call_CONTACTS, call_OUTGOING, call_RECORD, call_RESULT};

    public ApkinsonSQLiteHelper(Context context) {
        super(context, database_NAME, null, database_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create call table
        String CREATE_CALL_TABLE = "CREATE TABLE calls ( " + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "date TEXT, " + "duration INTEGER, " + "contacts INTEGER, " + "outgoing INTEGER, " + "record TEXT, " + "result TEXT )";
        db.execSQL(CREATE_CALL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop calls table if already exists
        db.execSQL("DROP TABLE IF EXISTS calls");
        this.onCreate(db);
    }

    public void addCall(Call call) {
        // get reference of the database
        SQLiteDatabase db = this.getWritableDatabase();

        // make values to be inserted
        ContentValues values = new ContentValues();
        values.put(call_DATE, call.getDateAsString());
        values.put(call_DURATION, call.getDurationSECONDS());
        values.put(call_CONTACTS, booleanToInt(call.isInCONTACTLIST()));
        values.put(call_OUTGOING, booleanToInt(call.isCallOUTGOING()));
        values.put(call_RECORD, call.getCallRECORD().toString());
        values.put(call_RESULT, call.getCallRESULT());

        // insert call
        db.insert(callTable, null, values);

        // close database
        db.close();
    }

    public Call getCall(int id) {
        // get reference of the database
        SQLiteDatabase db = this.getReadableDatabase();

        // get call query
        Cursor cursor = db.query(callTable,
                COLUMNS, " id = ?", new String[] { String.valueOf(id) }, null, null, null, null);

        // if results !=null, parse the first one
        if (cursor != null)
            cursor.moveToFirst();

        String dateAsString = cursor.getString(1);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = null;
        try {
            date = format.parse(dateAsString);
        } catch (ParseException e) {
            Log.e("ERROR", "Date parser error");
            e.printStackTrace();
        }
        int duration = cursor.getInt(2);
        boolean contacts = intToBoolean(cursor.getInt(3));
        boolean outgoing = intToBoolean(cursor.getInt(4));
        File record = new File(cursor.getString(5));
        String result = cursor.getString(6);
        Call call = new Call(date, contacts, outgoing, record);
        call.setDurationSECONDS(duration);
        call.setCallID(id);
        call.setCallRESULT(result);
        return call;
    }

    public ArrayList getAllCalls() {
        ArrayList calls = new ArrayList<>();

        // select call query
        String query = "SELECT  * FROM " + callTable;

        // get reference of the database
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // parse all results
        if (cursor.moveToFirst()) {
            do {
                String dateAsString = cursor.getString(1);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date date = null;
                try {
                    date = format.parse(dateAsString);
                } catch (ParseException e) {
                    Log.e("ERROR", "Date parser error");
                    e.printStackTrace();
                }
                int duration = cursor.getInt(2);
                boolean contacts = intToBoolean(cursor.getInt(3));
                boolean outgoing = intToBoolean(cursor.getInt(4));
                File record = new File(cursor.getString(5));
                String result = cursor.getString(6);
                Call call = new Call(date, contacts, outgoing, record);
                call.setDurationSECONDS(duration);
                call.setCallID(cursor.getInt(0));
                call.setCallRESULT(result);
                calls.add(call);

            } while (cursor.moveToNext());
        }
        return calls;
    }

    // Deleting a single call
    public void deleteCall(Call call) {

        // get reference of the database
        SQLiteDatabase db = this.getWritableDatabase();

        // delete call
        db.delete(callTable, call_ID + " = ?", new String[] { String.valueOf(call.getCallID()) });
        db.close();
    }

    private int booleanToInt(boolean bool){
        return (bool)? 1:0;
    }

    private boolean intToBoolean(int integer){
        return (integer==1);
    }
}