package com.keithandthegirl.app.sync;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.keithandthegirl.app.db.model.Event;
import com.keithandthegirl.app.db.model.EventConstants;
import com.keithandthegirl.app.db.model.Events;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by dmfrey on 1/21/15.
 */
public class EventsLoaderAsyncTask extends AsyncTask<Void, Void, Events> {

    private static final String TAG = EventsLoaderAsyncTask.class.getSimpleName();

    private KatgService mKatgService;

    private Context mContext;

    public EventsLoaderAsyncTask(Context context) {

        mContext = context;

        initializeClient();

    }

    @Override
    protected Events doInBackground( Void... params ) {
        Log.v( TAG, "doInBackground : enter" );

        try {

            Log.v( TAG, "doInBackground : exit" );
            return mKatgService.events();

        } catch( RetrofitError e ) {
            Log.e( TAG, "doInBackground : error", e );

            return null;
        }

    }

    @Override
    protected void onPostExecute( Events events ) {
        Log.v( TAG, "onPostExecute : enter" );

        if( null != events ) {
            Log.v( TAG, "onPostExecute : events is not null" );

            processEvents( events );

        }

        Log.v( TAG, "onPostExecute : exit" );
    }

    private void processEvents( Events events ) {
        Log.v( TAG, "processEvents : enter" );

        mContext.getContentResolver().delete( EventConstants.CONTENT_URI, null, null );

        String[] projection = new String[] { EventConstants._ID };

        ContentValues values;

        for( Event event : events.getEvents() ) {

            DateTime startDate = new DateTime( event.getStartDate() );
            startDate = startDate.withZone( DateTimeZone.UTC );

            DateTime endDate = new DateTime( event.getEndDate() );
            endDate = endDate.withZone( DateTimeZone.UTC );

            values = new ContentValues();
            values.put( EventConstants.FIELD_EVENTID, event.getEventId() );
            values.put( EventConstants.FIELD_TITLE, event.getTitle() );
            values.put( EventConstants.FIELD_LOCATION, event.getLocation() );
            values.put( EventConstants.FIELD_STARTDATE, startDate.getMillis() );
            values.put( EventConstants.FIELD_ENDDATE, endDate.getMillis() );
            values.put( EventConstants.FIELD_DETAILS, event.getDetails() );
            values.put( EventConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

            Cursor cursor = mContext.getContentResolver().query( EventConstants.CONTENT_URI, projection, EventConstants.FIELD_EVENTID + "=?", new String[] { event.getEventId() }, null );
            if( cursor.moveToFirst() ) {
                //Log.v( TAG, "processEvents : event iteration, updating existing entry" );

                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( EventConstants._ID ) );
                mContext.getContentResolver().update( ContentUris.withAppendedId( EventConstants.CONTENT_URI, id ), values, null, null );

            } else {
                //Log.v( TAG, "processEvents : event iteration, adding new entry" );

                mContext.getContentResolver().insert( EventConstants.CONTENT_URI, values );

            }
            cursor.close();

        }

        Log.e(TAG, "processEvents : exit");

    }

    private void initializeClient() {

        OkHttpClient client = new OkHttpClient();

        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        File cacheDirectory = new File( mContext.getCacheDir().getAbsolutePath(), "HttpCache" );
        try {
            Cache cache = new Cache( cacheDirectory, cacheSize );
            client.setCache( cache );
        } catch( IOException e ) { }

        Gson katgGson = new GsonBuilder()
                .setDateFormat("MM/dd/yyyy HH:mm")
                .create();

        RestAdapter katgRestAdapter = new RestAdapter.Builder()
                .setEndpoint( KatgService.KATG_URL )
                .setClient( new OkClient( client ) )
                .setConverter( new GsonConverter( katgGson ) )
                .build();

        mKatgService = katgRestAdapter.create( KatgService.class );

    }

}
