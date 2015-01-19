package com.keithandthegirl.app.sync;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.keithandthegirl.app.db.model.Detail;
import com.keithandthegirl.app.db.model.DetailConstants;
import com.keithandthegirl.app.db.model.Image;
import com.keithandthegirl.app.db.model.ImageConstants;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by dmfrey on 10/23/14.
 */
public class EpisodeDetailsAsyncTask extends AsyncTask<Void, Void, Detail> {

    private static final String TAG = EpisodeDetailsAsyncTask.class.getSimpleName();
    private static final String[] projection = null;

    public static final String COMPLETE_ACTION = "com.keithandthegirl.app.sync.episodeDetails.COMPLETE_ACTION";

    private KatgService mKatgService;

    private Context mContext;
    private final int mShowId;

    public EpisodeDetailsAsyncTask(final Context context, final int showId ) {

        mContext = context;
        mShowId = showId;

        initializeClient();
    }

    @Override
    protected Detail doInBackground( Void... params ) {
        return mKatgService.showDetails( mShowId, 1 );
    }

    @Override
    protected void onPostExecute( Detail detail ) {

        ContentValues values = new ContentValues();
        values.put( DetailConstants.FIELD_NOTES, detail.getNotes() );
        values.put( DetailConstants.FIELD_FORUMURL, detail.getForumUrl() );
        values.put( DetailConstants.FIELD_SHOWID, mShowId );
        values.put( DetailConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

        Cursor cursor = mContext.getContentResolver().query( DetailConstants.CONTENT_URI, projection, DetailConstants.FIELD_SHOWID + "=?", new String[] { String.valueOf( mShowId ) }, null );
        if( cursor.moveToFirst() ) {
            //Log.v( TAG, "processEpisodeDetails : detail iteration, updating existing entry" );

            Long id = cursor.getLong( cursor.getColumnIndexOrThrow( DetailConstants._ID ) );
            mContext.getContentResolver().update(ContentUris.withAppendedId(DetailConstants.CONTENT_URI, id), values, null, null);

        } else {
            //Log.v( TAG, "processEpisodeDetails : detail iteration, adding new entry" );

            mContext.getContentResolver().insert( DetailConstants.CONTENT_URI, values );

        }
        cursor.close();

        for( Image image : detail.getImages() ) {
            //Log.v(TAG, "processEpisodeDetails : image=" + image.toString());

            values = new ContentValues();
            values.put( ImageConstants._ID, image.getPictureId() );
            values.put( ImageConstants.FIELD_TITLE, image.getTitle() );
            values.put( ImageConstants.FIELD_DESCRIPTION, image.getDescription() );
            values.put( ImageConstants.FIELD_EXPLICIT, image.isExplicit() ? 1 : 0 );
            values.put( ImageConstants.FIELD_DISPLAY_ORDER, image.getDisplayOrder() );
            values.put( ImageConstants.FIELD_MEDIAURL, image.getMediaUrl() );
            values.put( ImageConstants.FIELD_SHOWID, mShowId );
            values.put( ImageConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

            cursor = mContext.getContentResolver().query( ContentUris.withAppendedId( ImageConstants.CONTENT_URI, image.getPictureId() ), null, null, null, null );
            if( cursor.moveToFirst() ) {
                //Log.v( TAG, "processEpisodeDetails : image iteration, updating existing entry" );

                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( ImageConstants._ID ) );
                mContext.getContentResolver().update( ContentUris.withAppendedId( ImageConstants.CONTENT_URI, id ), values, null, null );

            } else {
                //Log.v( TAG, "processEpisodeDetails : image iteration, adding new entry" );

                mContext.getContentResolver().insert( ImageConstants.CONTENT_URI, values );

            }
            cursor.close();

        }

        Intent completeIntent = new Intent();
        completeIntent.setAction( COMPLETE_ACTION );
        mContext.sendBroadcast( completeIntent );

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

    private String concatList( List<String> sList, String separator ) {
        Iterator<String> iter = sList.iterator();
        StringBuilder sb = new StringBuilder();

        while( iter.hasNext() ){
            sb.append( iter.next() ).append( iter.hasNext() ? separator : "" );
        }
        return sb.toString();
    }

}
