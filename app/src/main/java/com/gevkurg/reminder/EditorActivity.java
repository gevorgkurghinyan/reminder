package com.gevkurg.reminder;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.gevkurg.reminder.data.ReminderContract;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int EXISTING_REMINDER_LOADER = 0;

    private EditText nameEt;
    private DatePicker dueDateDp;
    private EditText notesEt;
    private Spinner prioritySp;
    private Spinner statusSp;

    private Uri currentReminderUri;
    private int priorityLevel = ReminderContract.ReminderEntry.PRIORITY_LOW;
    private int status = ReminderContract.ReminderEntry.STATUS_TODO;

    private boolean reminderHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the reminderHasChanged boolean to true.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            reminderHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.reminderToolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        // Enable the Up button
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        currentReminderUri = intent.getData();

        // If the intent DOES NOT contain a reminder content URI, then we know that we are
        // creating a new reminder.
        if (currentReminderUri == null) {
            // This is a new reminder, so change the app bar to say "Add a Reminder"
            setTitle(getString(R.string.editor_activity_title_new_reminder));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a reminder that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing reminder, so change app bar to say "Edit Reminder"
            setTitle(getString(R.string.editor_activity_title_edit_reminder));

            // Initialize a loader to read the reminder data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_REMINDER_LOADER, null, this);
        }

        nameEt = (EditText) findViewById(R.id.et_name);
        dueDateDp = (DatePicker) findViewById(R.id.datePicker);
        notesEt = (EditText) findViewById(R.id.et_notes);
        prioritySp = (Spinner) findViewById(R.id.sp_priorityLevel);
        statusSp = (Spinner) findViewById(R.id.sp_status);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        nameEt.setOnTouchListener(touchListener);
        dueDateDp.setOnTouchListener(touchListener);
        notesEt.setOnTouchListener(touchListener);
        prioritySp.setOnTouchListener(touchListener);
        statusSp.setOnTouchListener(touchListener);

        setupPriorityLevelSpinner();
        setupStatusSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_item_toolbar, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new reminder, hide the "Delete" menu item.
        if (currentReminderUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_deleteItem);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_saveItem:
                saveReminder();
                finish();
                return true;
            case R.id.action_deleteItem:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the reminder hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!reminderHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the reminder hasn't changed, continue with handling back button press
        if (!reminderHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all reminder attributes, define a projection that contains
        // all columns from the reminder table
        String[] projection = {
                ReminderContract.ReminderEntry._ID,
                ReminderContract.ReminderEntry.COLUMN_NAME,
                ReminderContract.ReminderEntry.COLUMN_DUE_DATE,
                ReminderContract.ReminderEntry.COLUMN_NOTES,
                ReminderContract.ReminderEntry.COLUMN_PRIORITY_LEVEL,
                ReminderContract.ReminderEntry.COLUMN_STATUS};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentReminderUri,     // Query the content URI for the current reminder
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of reminder attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ReminderContract.ReminderEntry.COLUMN_NAME);
            int dueDateColumnIndex = cursor.getColumnIndex(ReminderContract.ReminderEntry.COLUMN_DUE_DATE);
            int notesColumnIndex = cursor.getColumnIndex(ReminderContract.ReminderEntry.COLUMN_NOTES);
            int priorityColumnIndex = cursor.getColumnIndex(ReminderContract.ReminderEntry.COLUMN_PRIORITY_LEVEL);
            int statusColumnIndex = cursor.getColumnIndex(ReminderContract.ReminderEntry.COLUMN_STATUS);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String dueDate = cursor.getString(dueDateColumnIndex);
            String notes = cursor.getString(notesColumnIndex);
            int priorityLevel = cursor.getInt(priorityColumnIndex);
            int status = cursor.getInt(statusColumnIndex);

            // due date can be stored as a 3 different fields in the DB.
            String[] date = dueDate.split("\\.");

            // Update the views on the screen with the values from the database
            nameEt.setText(name);
            dueDateDp.updateDate(Integer.valueOf(date[2]), Integer.valueOf(date[0]), Integer.valueOf(date[1]));
            notesEt.setText(notes);

            // Priority level is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is LOW, 1 is MEDIUM, 2 is HIGH).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (priorityLevel) {
                case ReminderContract.ReminderEntry.PRIORITY_MEDIUM:
                    prioritySp.setSelection(1);
                    break;
                case ReminderContract.ReminderEntry.PRIORITY_HIGH:
                    prioritySp.setSelection(2);
                    break;
                default:
                    prioritySp.setSelection(0);
                    break;
            }

            switch (status) {
                case ReminderContract.ReminderEntry.STATUS_DONE:
                    statusSp.setSelection(1);
                    break;
                default:
                    statusSp.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameEt.setText("");
        notesEt.setText("");
        prioritySp.setSelection(0);
        statusSp.setSelection(0);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the reminder.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this reminder.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the reminder.
                deleteReminder();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the reminder.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the reminder in the database.
     */
    private void deleteReminder() {
        // Only perform the delete if this is an existing reminder.
        if (currentReminderUri != null) {
            // Call the ContentResolver to delete the reminder at the given content URI.
            // Pass in null for the selection and selection args because the currentReminderUri
            // content URI already identifies the reminder that we want.
            int rowsDeleted = getContentResolver().delete(currentReminderUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_reminder_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_reminder_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void setupPriorityLevelSpinner() {
        ArrayAdapter prioritySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        prioritySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        prioritySp.setAdapter(prioritySpinnerAdapter);

        prioritySp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.priority_low))) {
                        priorityLevel = ReminderContract.ReminderEntry.PRIORITY_LOW;
                    } else if (selection.equals(getString(R.string.priority_medium))) {
                        priorityLevel = ReminderContract.ReminderEntry.PRIORITY_MEDIUM;
                    } else {
                        priorityLevel = ReminderContract.ReminderEntry.PRIORITY_HIGH;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                priorityLevel = ReminderContract.ReminderEntry.PRIORITY_LOW;
            }
        });
    }

    private void setupStatusSpinner() {
        ArrayAdapter statusSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.status_array, android.R.layout.simple_spinner_item);
        statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        statusSp.setAdapter(statusSpinnerAdapter);

        statusSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.status_todo))) {
                        status = ReminderContract.ReminderEntry.STATUS_TODO;
                    } else {
                        status = ReminderContract.ReminderEntry.STATUS_DONE;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                status = ReminderContract.ReminderEntry.STATUS_TODO;
            }
        });
    }

    private void saveReminder() {

        String name = nameEt.getText().toString().trim();
        String dueDate = String.format("%s.%s.%s", dueDateDp.getMonth(), dueDateDp.getDayOfMonth(), dueDateDp.getYear());
        String notes = notesEt.getText().toString().trim();

        // Check if this is supposed to be a new reminder
        // and check if all the fields in the editor are blank
        if (currentReminderUri == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(notes)) {
            // Since no fields were modified, we can return early without creating a new reminder.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and reminder attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ReminderContract.ReminderEntry.COLUMN_NAME, name);
        values.put(ReminderContract.ReminderEntry.COLUMN_DUE_DATE, dueDate);
        values.put(ReminderContract.ReminderEntry.COLUMN_NOTES, notes);
        values.put(ReminderContract.ReminderEntry.COLUMN_PRIORITY_LEVEL, priorityLevel);
        values.put(ReminderContract.ReminderEntry.COLUMN_STATUS, status);

        try {
            // Determine if this is a new or existing reminder by checking if currentReminderUri is null or not
            if (currentReminderUri == null) {
                // This is a NEW reminder, so insert a new reminder into the provider,
                // returning the content URI for the new reminder.
                Uri newUri = getContentResolver().insert(ReminderContract.ReminderEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_reminder_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_reminder_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING reminder, so update the reminder with content URI: currentReminderUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because currentReminderUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(currentReminderUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_reminder_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_reminder_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Failed to save the reminder.", ex);
        }
    }
}