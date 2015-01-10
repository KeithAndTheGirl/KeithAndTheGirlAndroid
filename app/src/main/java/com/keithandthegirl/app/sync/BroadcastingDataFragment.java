package com.keithandthegirl.app.sync;

import android.content.ContentUris;
import android.content.ContentValues;
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
import com.keithandthegirl.app.db.model.Live;
import com.keithandthegirl.app.db.model.LiveConstants;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by dmfrey on 12/31/14.
 */
public class BroadcastingDataFragment extends Fragment {

    private static final String TAG = BroadcastingDataFragment.class.getSimpleName();

    KatgService katgService;
    private boolean loading = false;

    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        Log.v( TAG, "onCreateView : enter" );

        initializeClient();
        new BroadcastingLoaderAsyncTask().execute();

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

    public boolean isLoading() {
        return loading;
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

    private void processBroadcasting( Live live ) {
        Log.v( TAG, "processBroadcasting : enter" );

        ContentValues values = new ContentValues();
        values.put( LiveConstants.FIELD_BROADCASTING, live.isBroadcasting() ? 1 : 0 );
        values.put( LiveConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

        getActivity().getContentResolver().update( ContentUris.withAppendedId( LiveConstants.CONTENT_URI, 1 ), values, null, null );

        Log.v( TAG, "processBroadcasting : exit" );
    }

    private class BroadcastingLoaderAsyncTask extends AsyncTask<Void, Void, Live> {

        private String TAG = BroadcastingLoaderAsyncTask.class.getSimpleName();

        @Override
        protected Live doInBackground( Void... params ) {
            Log.v( TAG, "doInBackground : enter" );

            loading = true;

            try {
                Live live = katgService.broadcasting();

                Log.v( TAG, "doInBackground : exit" );
                return live;
            } catch( RetrofitError e ) {

                Log.v( TAG, "doInBackground : error", e );
                return null;
            }
        }

        @Override
        protected void onPostExecute( Live live ) {
            Log.v( TAG, "onPostExecute : enter" );

            if( null != live ) {
                Log.v( TAG, "onPostExecute : process broadcasting status" );

                processBroadcasting( live );

            }

            loading = false;

            Log.v( TAG, "onPostExecute : exit" );
        }

    }

}
