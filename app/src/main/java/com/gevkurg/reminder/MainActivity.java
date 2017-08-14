package com.gevkurg.reminder;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gevkurg.reminder.data.ReminderContract;
import com.gevkurg.reminder.data.ReminderCursorAdapter;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the reminder data loader */
    private static final int REMINDER_LOADER = 0;

    private ListView itemsList;
    ReminderCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.reminderToolbar);
        setSupportActionBar(toolbar);

        itemsList = (ListView) findViewById(R.id.lvItems);

        cursorAdapter = new ReminderCursorAdapter(this, null, 0);
        itemsList.setAdapter(cursorAdapter);

        itemsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific reminder that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ReminderEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.gevkurg.reminder/reminders/2"
                // if the reminder with ID 2 was clicked on.
                Uri currentReminderUri = ContentUris.withAppendedId(ReminderContract.ReminderEntry.CONTENT_URI, l);

                // Set the URI on the data field of the intent
                intent.setData(currentReminderUri);

                // Launch the {@link EditorActivity} to display the data for the current reminder.
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(REMINDER_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addItem:
                Intent intent = new Intent(this, EditorActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_delete_all_entries:
                deleteAllReminders();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((CursorAdapter) itemsList.getAdapter()).getCursor().close();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ReminderContract.ReminderEntry._ID,
                ReminderContract.ReminderEntry.COLUMN_NAME,
                ReminderContract.ReminderEntry.COLUMN_PRIORITY_LEVEL };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,                         // Parent activity context
                ReminderContract.ReminderEntry.CONTENT_URI,   // Provider content URI to query
                projection,                                   // Columns to include in the resulting Cursor
                null,                                         // No selection clause
                null,                                         // No selection arguments
                null);                                        // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ReminderCursorAdapter} with this new cursor containing updated reminder data
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        cursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all reminders in the database.
     */
    private void deleteAllReminders() {
        int rowsDeleted = getContentResolver().delete(ReminderContract.ReminderEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from reminder database");
    }
}