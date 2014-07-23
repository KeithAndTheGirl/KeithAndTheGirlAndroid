package com.keithandthegirl.app.ui.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * ShowNextEventWidget created by Jeff Alexander 7/22/2014
 * Copyright Keith and the Girl, 2014
 */
public class ShowNextEventWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_show_next_live_event);

        String[] projection = new String[] { Event.FIELD_TITLE, Event.FIELD_STARTDATE};
        String selectionClause = Event.FIELD_STARTDATE + " > ?";
        String sortOrder = Event.FIELD_STARTDATE + " ASC LIMIT 1";

        Cursor cursor = context.getContentResolver().query(Event.CONTENT_URI,
                projection,
                selectionClause,
                new String[] { String.valueOf(Calendar.getInstance().getTimeInMillis()) },
                sortOrder);

        // if we have an event update the widget UI
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndex(Event.FIELD_TITLE));
            views.setTextViewText(R.id.eventTitleTextView, title);

            long start = cursor.getLong( cursor.getColumnIndex( Event.FIELD_STARTDATE ) );
            Date date = new Date(start);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM d, h:mm aaa");
            views.setTextViewText(R.id.eventTimeTextView, simpleDateFormat.format(date));

        } else {
            views.setTextViewText(R.id.eventTimeTextView, "");
            views.setTextViewText(R.id.eventTitleTextView, "No upcoming Events");
        }
        cursor.close();

//        cursor = context.getContentResolver().query()

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}