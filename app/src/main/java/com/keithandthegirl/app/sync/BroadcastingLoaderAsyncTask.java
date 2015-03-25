package com.keithandthegirl.app.sync;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by dmfrey on 1/19/15.
 */
public class BroadcastingLoaderAsyncTask extends AsyncTask<Void, Void, Live> {

    private String TAG = BroadcastingLoaderAsyncTask.class.getSimpleName();

    Context mContext;
    KatgService mKatgService;

    boolean loading = false;

    public BroadcastingLoaderAsyncTask( Context context ) {

        mContext = context;

        initializeClient();

    }

    @Override
    protected Live doInBackground( Void... params ) {
        Log.v(TAG, "doInBackground : enter");

        loading = true;

        try {
            Live live = mKatgService.broadcasting();

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

    private void processBroadcasting( Live live ) {
        Log.v( TAG, "processBroadcasting : enter" );

        Log.v( TAG, "processBroadcasting : live=" + live );

        ContentValues values = new ContentValues();
        values.put( LiveConstants.FIELD_BROADCASTING, live.isBroadcasting() ? 1 : 0 );
        values.put( LiveConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

        mContext.getContentResolver().update( ContentUris.withAppendedId(LiveConstants.CONTENT_URI, 1), values, null, null );

        Log.v( TAG, "processBroadcasting : exit" );
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
