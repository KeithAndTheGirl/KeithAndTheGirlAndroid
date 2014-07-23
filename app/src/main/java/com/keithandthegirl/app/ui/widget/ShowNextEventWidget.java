package com.keithandthegirl.app.ui.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.DatabaseHelper;
import com.keithandthegirl.app.db.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class ShowNextEventWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.show_next_live_event_widget);

        // get the next event
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Cursor cursor = databaseHelper.getNextEvent();
        if (cursor != null && cursor.moveToFirst()) {
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

        if (cursor != null) {
            cursor.close();
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


