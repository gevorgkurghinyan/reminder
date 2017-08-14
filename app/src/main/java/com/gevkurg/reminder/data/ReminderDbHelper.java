package com.gevkurg.reminder.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Database helper for Reminder app. Manages database creation and versioning.
 */
public class ReminderDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = ReminderDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "reminder2.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ReminderDbHelper}.
     *
     * @param context of the app
     */
    public ReminderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the reminder table
        String SQL_CREATE_REMINDERS_TABLE =  "CREATE TABLE " + ReminderContract.ReminderEntry.TABLE_NAME + " ("
                + ReminderContract.ReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ReminderContract.ReminderEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ReminderContract.ReminderEntry.COLUMN_DUE_DATE + " TEXT, "
                + ReminderContract.ReminderEntry.COLUMN_NOTES + " TEXT, "
                + ReminderContract.ReminderEntry.COLUMN_PRIORITY_LEVEL + " INTEGER NOT NULL DEFAULT 0, "
                + ReminderContract.ReminderEntry.COLUMN_STATUS + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_REMINDERS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}