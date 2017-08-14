package com.gevkurg.reminder.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gevkurg.reminder.R;


public class ReminderCursorAdapter extends CursorAdapter {
    private LayoutInflater cursorInflater;

    public ReminderCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.reminder_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvPriority = view.findViewById(R.id.tv_priorityLevel);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME));
        int priority = cursor.getInt(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_PRIORITY_LEVEL));

        tvName.setText(name);
        tvPriority.setText(getPriorityLevel(priority, context));
    }

    private String getPriorityLevel(int level, Context context) {
        switch (level) {
            case 0:
                return context.getString(R.string.priority_low);
            case 1:
                return context.getString(R.string.priority_medium);
            case 2:
                return context.getString(R.string.priority_high);
            default:
                return context.getString(R.string.priority_low);
        }
    }
}