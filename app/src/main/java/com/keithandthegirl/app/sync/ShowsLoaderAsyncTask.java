package com.keithandthegirl.app.sync;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by dmfrey on 1/18/15.
 */
public class ShowsLoaderAsyncTask extends AsyncTask<Void, Void, List<Show>> {

    private String TAG = ShowsLoaderAsyncTask.class.getSimpleName();

    Context mContext;
    KatgService mKatgService;

    public ShowsLoaderAsyncTask( Context context ) {

        mContext = context;

        initializeClient();

    }

    @Override
    protected List<Show> doInBackground( Void... params ) {
        Log.v(TAG, "doInBackground : enter");

        try {
            List<Show> shows = mKatgService.seriesOverview();

            Log.v( TAG, "doInBackground : exit" );
            return shows;
        } catch( RetrofitError e ) {

            Log.v( TAG, "doInBackground : error", e );
            return null;
        }

    }

    @Override
    protected void onPostExecute( List<Show> shows ) {
        Log.v( TAG, "onPostExecute : enter" );

        if( null != shows && !shows.isEmpty() ) {
            Log.v( TAG, "onPostExecute : process downloaded shows" );

            processShows( shows );

        }

        Log.v( TAG, "onPostExecute : exit" );
    }

    private void processShows( List<Show> shows ) {
        Log.v( TAG, "processShows : enter" );

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        String[] projection = new String[] { ShowConstants._ID };
        ContentValues values;

        for( Show show : shows ) {
            Log.v( TAG, "processShows : show=" + show.toString() );

            values = new ContentValues();
            values.put( ShowConstants._ID, show.getShowNameId() );
            values.put( ShowConstants.FIELD_NAME, show.getName() );
            values.put( ShowConstants.FIELD_PREFIX, show.getPrefix() );
            values.put( ShowConstants.FIELD_VIP, show.isVip() ? 1 : 0 );
            values.put( ShowConstants.FIELD_SORTORDER, show.getSortOrderAsInt() );
            values.put( ShowConstants.FIELD_DESCRIPTION, show.getDescription() );
            values.put( ShowConstants.FIELD_COVERIMAGEURL, show.getCoverImageUrl() );
            values.put( ShowConstants.FIELD_COVERIMAGEURL_SQUARED, show.getCoverImageUrlSquared() );
            values.put( ShowConstants.FIELD_COVERIMAGEURL_100, show.getCoverImageUrl100() );
            values.put( ShowConstants.FIELD_COVERIMAGEURL_200, show.getCoverImageUrl200() );
            values.put( ShowConstants.FIELD_FORUMURL, show.getForumUrl() );
            values.put( ShowConstants.FIELD_PREVIEWURL, show.getPreviewUrl() );
            values.put( ShowConstants.FIELD_EPISODE_COUNT, show.getEpisodeCount() );
            values.put( ShowConstants.FIELD_EPISODE_COUNT_MAX, show.getEpisodeNumberMax() );
            values.put( ShowConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

            Cursor cursor = mContext.getContentResolver().query( ContentUris.withAppendedId(ShowConstants.CONTENT_URI, show.getShowNameId()), projection, null, null, null );
            if( cursor.moveToFirst() ) {
                Log.v( TAG, "processShows : show iteration, updating existing entry" );

                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( ShowConstants._ID ) );
                ops.add(
                        ContentProviderOperation
                                .newUpdate(ContentUris.withAppendedId(ShowConstants.CONTENT_URI, id))
                                .withValues(values)
                                .build()
                );

            } else {
                Log.v( TAG, "processShows : show iteration, adding new entry" );

                mContext.getContentResolver().insert( ShowConstants.CONTENT_URI, values );
                ops.add(
                        ContentProviderOperation
                                .newInsert(ShowConstants.CONTENT_URI)
                                .withValues(values)
                                .build()
                );

            }
            cursor.close();

        }

        try {

            mContext.getContentResolver().applyBatch( KatgProvider.AUTHORITY, ops );

        } catch( Exception e ) {

            // Display warning
            CharSequence txt = mContext.getString( R.string.processShowsFailure );
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText( mContext, txt, duration );
            toast.show();

            // Log exception
            Log.e( TAG, "processShows : error processing shows", e );
        }

        Log.v( TAG, "processShows : exit" );
    }

    private void initializeClient() {
        Log.v( TAG, "initializeClient : enter" );

        OkHttpClient client = new OkHttpClient();

        int cacheSize = 3 * 1024 * 1024; // 3 MiB
        File cacheDirectory = new File( mContext.getCacheDir().getAbsolutePath(), "HttpCache" );

        Cache cache = new Cache( cacheDirectory, cacheSize );
        client.setCache( cache );

        Gson katgGson = new GsonBuilder()
                .setDateFormat( "MM/dd/yyyy HH:mm" )
                .create();

        RestAdapter katgRestAdapter = new RestAdapter.Builder()
                .setEndpoint( KatgService.KATG_URL )
                .setClient( new OkClient( client ) )
                .setConverter( new GsonConverter( katgGson ) )
//            .setLogLevel( RestAdapter.//LogLevel.FULL )
                .build();

        mKatgService = katgRestAdapter.create( KatgService.class );

        Log.v( TAG, "initializeClient : exit" );
    }

}
