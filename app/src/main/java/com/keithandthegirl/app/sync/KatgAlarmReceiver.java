package com.keithandthegirl.app.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.keithandthegirl.app.db.model.ShowConstants;

/**
 * Created by dmfrey on 1/19/15.
 */
public class KatgAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = KatgAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive( Context context, Intent intent ) {
        Log.v( TAG, "onRecieve : enter" );

        String[] projection = new String[] { ShowConstants._ID };
        String selection = null;
        String[] selectionArgs = null;

        Cursor cursor = context.getContentResolver().query( ShowConstants.CONTENT_URI, projection, selection, selectionArgs, ShowConstants.FIELD_SORTORDER );
        while( cursor.moveToNext() ) {

            long showNameId = cursor.getLong( cursor.getColumnIndex( ShowConstants._ID ) );

            new EpisodeListAsyncTask( context, (int) showNameId, -1, -1, 10, true ).execute();

        }
        cursor.close();

        new BroadcastingLoaderAsyncTask( context ).execute();
        new EventsLoaderAsyncTask( context ).execute();
        new YoutubeLoaderAsyncTask( context ).execute();

        Log.v( TAG, "onRecieve : exit" );
    }

}
