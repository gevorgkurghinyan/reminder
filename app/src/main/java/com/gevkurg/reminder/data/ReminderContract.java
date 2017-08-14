package com.gevkurg.reminder.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public final class ReminderContract {

    private ReminderContract(){}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.gevkurg.reminder";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.gevkurg.reminder/reminders/ is a valid path for
     * looking at reminder data.
     */
    public static final String PATH_REMINDERS = "reminders";

    /**
     * Inner class that defines constant values for the reminder database table.
     * Each entry in the table represents a single reminder.
     */
    public static final class ReminderEntry implements BaseColumns {

        /** The content URI to access the reminder data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_REMINDERS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of reminders.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REMINDERS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single reminder.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REMINDERS;

        /** Name of database table for reminders */
        public final static String TABLE_NAME = "reminders";

        /**
         * Unique ID number for the reminder (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the reminder.
         *
         * Type: TEXT
         */
        public final static String COLUMN_NAME ="name";

        /**
         * Due date of the reminder.
         *
         * Type: TEXT
         */
        public final static String COLUMN_DUE_DATE = "dueDate";

        /**
         * Notes of the reminder.
         *
         * Type: TEXT
         */
        public final static String COLUMN_NOTES ="notes";

        /**
         * Priority level.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRIORITY_LEVEL = "priorityLevel";

        /**
         * Status.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_STATUS = "status";

        public static final int PRIORITY_LOW = 0;
        public static final int PRIORITY_MEDIUM = 1;
        public static final int PRIORITY_HIGH = 2;

        public static final int STATUS_TODO = 0;
        public static final int STATUS_DONE = 1;
    }
}