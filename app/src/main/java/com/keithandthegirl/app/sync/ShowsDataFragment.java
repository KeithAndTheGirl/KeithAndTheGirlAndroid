package com.keithandthegirl.app.sync;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by dmfrey on 12/31/14.
 */
public class ShowsDataFragment extends Fragment {

    private static final String TAG = ShowsDataFragment.class.getSimpleName();

    KatgService katgService;
    private boolean loading = false;

    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        Log.v( TAG, "onCreateView : enter" );

        initializeClient();
        new ShowsLoaderAsyncTask().execute();

        Log.v( TAG, "onCreateView : exit" );
        return null;
    }

    @Override
    public void onDestroyView() {
        Log.v( TAG, "onDestroyView : enter" );

        katgService = null;

        super.onDestroyView();

        Log.v( TAG, "onDestroyView : exit" );
    }

    private void initializeClient() {
        Log.v( TAG, "initializeClient : enter" );

        OkHttpClient client = new OkHttpClient();

        int cacheSize = 3 * 1024 * 1024; // 3 MiB
        File cacheDirectory = new File( getActivity().getCacheDir().getAbsolutePath(), "HttpCache" );

        try {
            Cache cache = new Cache( cacheDirectory, cacheSize );
            client.setCache( cache );
        } catch( IOException e ) { }

        Gson katgGson = new GsonBuilder()
                .setDateFormat( "MM/dd/yyyy HH:mm" )
                .create();

        RestAdapter katgRestAdapter = new RestAdapter.Builder()
                .setEndpoint( KatgService.KATG_URL )
                .setClient( new OkClient( client ) )
                .setConverter( new GsonConverter( katgGson ) )
//            .setLogLevel( RestAdapter.//LogLevel.FULL )
                .build();

        katgService = katgRestAdapter.create( KatgService.class );

        Log.v( TAG, "initializeClient : exit" );
    }

    private void processShows( List<Show> shows ) {
        Log.v( TAG, "processShows : enter" );

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

            Cursor cursor = getActivity().getContentResolver().query( ContentUris.withAppendedId( ShowConstants.CONTENT_URI, show.getShowNameId() ), projection, null, null, null );
            if( cursor.moveToFirst() ) {
                Log.v( TAG, "processShows : show iteration, updating existing entry" );

                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( ShowConstants._ID ) );
                getActivity().getContentResolver().update( ContentUris.withAppendedId( ShowConstants.CONTENT_URI, id ), values, null, null );

            } else {
                Log.v( TAG, "processShows : show iteration, adding new entry" );

                getActivity().getContentResolver().insert( ShowConstants.CONTENT_URI, values );

            }
            cursor.close();

        }

        Log.v( TAG, "processShows : exit" );
    }

    private class ShowsLoaderAsyncTask extends AsyncTask<Void, Void, List<Show>> {

        private String TAG = ShowsLoaderAsyncTask.class.getSimpleName();

        @Override
        protected List<Show> doInBackground( Void... params ) {
            Log.v( TAG, "doInBackground : enter" );

            loading = true;

            List<Show> shows = katgService.seriesOverview();

            Log.v( TAG, "doInBackground : exit" );
            return shows;
        }

        @Override
        protected void onPostExecute( List<Show> shows ) {
            Log.v( TAG, "onPostExecute : enter" );

            if( null != shows && !shows.isEmpty() ) {
                Log.v( TAG, "onPostExecute : process downloaded shows" );

                processShows( shows );

            }

            loading = false;

            Log.v( TAG, "onPostExecute : exit" );
        }

    }

}
