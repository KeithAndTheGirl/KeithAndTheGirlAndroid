package com.keithandthegirl.app.db.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.keithandthegirl.app.db.model.Detail;
import com.keithandthegirl.app.db.model.Endpoint;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.EpisodeGuests;
import com.keithandthegirl.app.db.model.Event;
import com.keithandthegirl.app.db.model.Guest;
import com.keithandthegirl.app.db.model.Image;
import com.keithandthegirl.app.db.model.Live;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.db.model.WorkItem;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 *
 * Created by dmfrey on 3/10/14.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();

    private static final DateTimeFormatter format = DateTimeFormat.forPattern( "MM/dd/yyyy HH:mm" ).withZone( DateTimeZone.forTimeZone( TimeZone.getTimeZone( "America/New_York" ) ) );
    private static final DateTimeFormatter formata = DateTimeFormat.forPattern( "M/d/yyyy hh:mm:ss a" ).withZone( DateTimeZone.forTimeZone( TimeZone.getTimeZone( "America/New_York" ) ) );

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;

    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    SharedPreferences mSharedPreferences;
    Context mContext;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter( Context context, boolean autoInitialize ) {
        super( context, autoInitialize );

        mContext = context;

        updateConnectedFlags( context );

        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );

    }


    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter( Context context, boolean autoInitialize, boolean allowParallelSyncs ) {
        super( context, autoInitialize, allowParallelSyncs );

        mContext = context;

        updateConnectedFlags( context );

        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );

    }

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    private void updateConnectedFlags( Context context ) {

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if( null != activeInfo && activeInfo.isConnected() ) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }

        enableHttpResponseCache();
    }

    /*
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync( Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult ) {
        Log.i(TAG, "onPerformSync : enter");

        /*
         * Put the data transfer code here.
         */
        SyncResult result = new SyncResult();
        try {

            DateTime now = new DateTime( DateTimeZone.UTC );

            List<Job> jobs = new ArrayList<Job>();
            Cursor cursor = provider.query( WorkItem.CONTENT_URI, null, null, null, null );
            while( cursor.moveToNext() ) {

                Job job = new Job();

                Long id = cursor.getLong( cursor.getColumnIndex( WorkItem._ID ) );
                job.setId( id );

                WorkItem.Frequency wtype = WorkItem.Frequency.valueOf( cursor.getString( cursor.getColumnIndex( WorkItem.FIELD_FREQUENCY ) ) );

                WorkItem.Download dtype = WorkItem.Download.valueOf( cursor.getString( cursor.getColumnIndex( WorkItem.FIELD_DOWNLOAD ) ) );
                job.setDownload( dtype );

                Endpoint.Type type = Endpoint.Type.valueOf( cursor.getString( cursor.getColumnIndex( WorkItem.FIELD_ENDPOINT ) ) );
                job.setType( type );

                String address = cursor.getString( cursor.getColumnIndex( WorkItem.FIELD_ADDRESS ) );
                String parameters = cursor.getString( cursor.getColumnIndex( WorkItem.FIELD_PARAMETERS ) );
                if( dtype.equals( WorkItem.Download.JPG ) ) {
                    job.setUrl( address );
                    job.setFilename( parameters );
                } else {
                    job.setUrl( address + parameters );
                }

                WorkItem.Status status = WorkItem.Status.valueOf( cursor.getString( cursor.getColumnIndex( WorkItem.FIELD_STATUS ) ) );
                job.setStatus( status );

                DateTime lastRun = new DateTime( DateTimeZone.UTC );
                long lastRunMs = cursor.getLong( cursor.getColumnIndex( WorkItem.FIELD_LAST_RUN ) );
                if( lastRunMs > 0 ) {
                    lastRun = new DateTime( lastRunMs );
                }

                switch( wtype ) {

                    case ON_DEMAND:
                        if( !status.equals( WorkItem.Status.OK ) ) {
                            Log.v( TAG, "onPerformSync : adding On Demand job" );

                            jobs.add( job );
                        }

                        break;

                    case ONCE:
                        if( !status.equals( WorkItem.Status.OK ) ) {
                            Log.v( TAG, "onPerformSync : adding One Time job" );

                            jobs.add( job );
                        }

                        break;

                    case HOURLY:
                        if( status.equals( WorkItem.Status.NEVER ) ) {
                            Log.v( TAG, "onPerformSync : adding Hourly job, never run" );

                            jobs.add( job );

                        } else {
                            Log.v( TAG, "onPerformSync : adding Hourly job" );

                            if( Minutes.minutesBetween( lastRun, now ).getMinutes() >= 60 ) {

                                jobs.add( job );

                            }

                        }

                        break;

                    case DAILY:
                        if( status.equals( WorkItem.Status.NEVER ) ) {
                            Log.v( TAG, "onPerformSync : adding Daily job, never run" );

                            jobs.add( job );
                        } else {
                            Log.v( TAG, "onPerformSync : adding Daily job" );

                            if( Days.daysBetween( lastRun, now ).getDays() >= 1 ) {

                                jobs.add( job );

                            }

                        }

                        break;

                    case WEEKLY:
                        if( status.equals( WorkItem.Status.NEVER ) ) {
                            Log.v( TAG, "onPerformSync : adding Weekly job, never run" );

                            jobs.add( job );
                        } else {
                            Log.v( TAG, "onPerformSync : adding Weekly job" );

                            if( Days.daysBetween( lastRun, now ).getDays() >= 7 ) {

                                jobs.add( job );

                            }

                        }

                        break;
                }



            };
            cursor.close();

            Log.i( TAG, "onPerformSync : " + jobs.size() + " scheduled to run" );
            executeJobs( provider, jobs );

        } catch( RemoteException e ) {
            Log.e( TAG, "onPerformSync : error, RemoteException", e );

            result.hasHardError();
        } catch( IOException e ) {
            Log.e( TAG, "onPerformSync : error, IOException", e );

            result.hasHardError();
        }

        Log.i( TAG, "onPerformSync : exit" );
    }

    private void executeJobs( ContentProviderClient provider, List<Job> jobs ) throws RemoteException, IOException {
        Log.v( TAG, "executeJobs : enter" );

        if( !jobs.isEmpty() ) {

            for( Job job : jobs ) {

                switch( job.getType() ) {

                    case OVERVIEW:
                        Log.v( TAG, "runScheduledWorkItems : refreshing shows" );

                        getShows( provider, job );

                        break;

                    case EVENTS:
                        Log.v( TAG, "runScheduledWorkItems : refreshing events" );

                        getEvents( provider, job );

                        break;

                    case LIVE:
                        Log.v( TAG, "runScheduledWorkItems : refreshing live status" );

                        getLives( provider, job );

                        break;

                    case LIST:
                        Log.v( TAG, "runScheduledWorkItems : refreshing episode list" );

                        getEpisodes( provider, job );

                        break;

                    case RECENT:
                        Log.v( TAG, "runScheduledWorkItems : refreshing recent episodes" );

                        getRecentEpisodes( provider, job );

                        break;

                    case IMAGE:
                        Log.v( TAG, "runScheduledWorkItems : refreshing images" );

//                        saveImage( provider, job );

                        break;

                    case DETAILS:
                        Log.v( TAG, "runScheduledWorkItems : refreshing episode details" );

                        getEpisodeDetails( provider, job );

                        break;

                    default:
                        Log.w( TAG, "runScheduledWorkItems : Scheduled '" + job.getType().name() + "' not supported" );

                }
            }
        }

        Log.v( TAG, "executeJobs : exit" );
    }

    private void getShows( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        Log.v(TAG, "getShows : enter");

        try {

            if( wifiConnected || mobileConnected ) {
                Log.v( TAG, "getShows : network is available" );

                JSONArray jsonArray = loadJsonArrayFromNetwork( job );
                Log.i( TAG, "getShows : jsonArray=" + jsonArray.toString() );
                processShows( jsonArray, provider, job );
            }

        } catch( Exception e ) {
            Log.e(TAG, "getShows : error", e);
        }

        Log.v( TAG, "getShows : exit" );
    }

    private void getEvents( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        Log.v( TAG, "getEvents : enter" );

        try {

            if( wifiConnected || mobileConnected ) {
                Log.v( TAG, "getEvents : network is available" );

                JSONObject json = loadJsonFromNetwork( job );
                Log.i( TAG, "getEvents : json=" + json.toString() );
                processEvents(json, provider, job);
            }

        } catch( Exception e ) {
            Log.e(TAG, "getEvents : error", e);
        }

        Log.v( TAG, "getEvents : exit" );
    }

    private void getLives( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        Log.v(TAG, "getLives : enter");

        try {

            if( wifiConnected || mobileConnected ) {
                Log.v( TAG, "getEvents : network is available" );

                JSONObject json = loadJsonFromNetwork(job);
                Log.i( TAG, "getLives : json=" + json.toString() );
                processLives(json, provider, job);
            }

        } catch( Exception e ) {
            Log.e(TAG, "getLives : error", e);
        }

        Log.v( TAG, "getLives : exit" );
    }

    private void getEpisodes( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        Log.v( TAG, "getEpisodes : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItem._ID, job.getId() );
        update.put( WorkItem.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {

            if( wifiConnected || mobileConnected ) {
                Log.v( TAG, "getEpisodes : network is available" );

                JSONArray jsonArray = loadJsonArrayFromNetwork( job );
                Log.i( TAG, "getEpisodes : jsonArray=" + jsonArray.toString() );
                processEpisodes(jsonArray, provider, job.getType());
            }

            update.put( WorkItem.FIELD_ETAG, job.getEtag() );
            update.put( WorkItem.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItem.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e(TAG, "getEpisodes : error", e);

            update.put(WorkItem.FIELD_STATUS, WorkItem.Status.FAILED.name());
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItem.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "getEpisodes : exit" );
    }

    private void getEpisodeDetails( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        Log.v( TAG, "getEpisodeDetails : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItem._ID, job.getId() );
        update.put( WorkItem.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {

            if( wifiConnected || mobileConnected ) {
                Log.v( TAG, "getEpisodeDetails : network is available" );

                Uri uri = Uri.parse( job.getUrl() );
                String showId = uri.getQueryParameter( "showid" );

                JSONObject json = loadJsonFromNetwork( job );
                if( null != json ) {
                    Log.i( TAG, "getEpisodeDetails : json=" + json.toString() );
                    processEpisodeDetails( json, provider, Integer.parseInt( showId ) );
                }
            }

            update.put( WorkItem.FIELD_ETAG, job.getEtag() );
            update.put( WorkItem.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItem.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e(TAG, "getEpisodeDetails : error", e);

            update.put( WorkItem.FIELD_STATUS, WorkItem.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItem.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "getEpisodeDetails : exit" );
    }

    private void getRecentEpisodes( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        Log.v( TAG, "getRecentEpisodes : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItem._ID, job.getId() );
        update.put(WorkItem.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis());

        try {

            if( wifiConnected || mobileConnected ) {
                Log.v(TAG, "getRecentEpisodes : network is available");

                JSONArray jsonArray = loadJsonArrayFromNetwork(job);
                Log.i( TAG, "getRecentEpisodes : jsonArray=" + jsonArray.toString() );
                processEpisodes(jsonArray, provider, job.getType());
            }

            update.put( WorkItem.FIELD_ETAG, job.getEtag() );
            update.put( WorkItem.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItem.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e(TAG, "getRecentEpisodes : error", e);

            update.put(WorkItem.FIELD_STATUS, WorkItem.Status.FAILED.name());
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItem.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "getRecentEpisodes : exit" );
    }

    private void getShowDetails( ContentProviderClient provider, int showId ) throws RemoteException, IOException {
        Log.v( TAG, "getShowDetails : enter" );

        String address = "";
        Cursor cursor = provider.query( Endpoint.CONTENT_URI, null, Endpoint.FIELD_TYPE + "=?", new String[] { Endpoint.Type.DETAILS.name() }, null );
        while( cursor.moveToNext() ) {

            address = cursor.getString( cursor.getColumnIndex( Endpoint.FIELD_URL ) );

        }
        cursor.close();

        try {

            if( wifiConnected || mobileConnected ) {
                Log.v( TAG, "getShowDetails : network is available" );

                Job job = new Job();
                job.setUrl( address + "?showid=" + showId );

                JSONObject json = loadJsonFromNetwork( job );
                Log.i( TAG, "getShowDetails : json=" + json.toString() );
                processEpisodeDetails(json, provider, showId);
            }

        } catch( Exception e ) {
            Log.e(TAG, "getShowDetails : error", e);
        }

        Log.v( TAG, "getShowDetails : exit" );
    }

    private JSONObject loadJsonFromNetwork( Job job ) throws IOException, JSONException {
        Log.v( TAG, "loadJsonFromNetwork : enter" );

        JSONObject json = null;
        InputStream stream = null;

        try {

            stream = downloadUrl( job );

            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader( new InputStreamReader( stream, "UTF-8" ), 8 );
            StringBuilder sb = new StringBuilder();

            String line = null;
            while( ( line = reader.readLine() ) != null ) {
                sb.append( line + "\n" );
            }
            json = new JSONObject( sb.toString() );

            job.setStatus( WorkItem.Status.OK );

        } catch( NullPointerException e ) {
            Log.e( TAG, "loadJsonFromNetwork : error", e );

            job.setStatus( WorkItem.Status.FAILED );
        } finally {

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            if( null != stream  ) {
                stream.close();
            }

        }

        Log.v( TAG, "loadJsonFromNetwork : exit" );
        return json;
    }

    private JSONArray loadJsonArrayFromNetwork( Job job ) throws IOException, JSONException {
        Log.v( TAG, "loadJsonArrayFromNetwork : enter" );

        JSONArray jsonArray = null;
        InputStream stream = null;

        try {

            stream = downloadUrl( job );

            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader( new InputStreamReader( stream, "UTF-8" ), 8 );
            StringBuilder sb = new StringBuilder();

            String line = null;
            while( ( line = reader.readLine() ) != null ) {
                sb.append( line + "\n" );
            }
            jsonArray = new JSONArray( sb.toString() );

            job.setStatus( WorkItem.Status.OK );

        } finally {

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            if( null != stream  ) {
                stream.close();
            }

        }

        Log.v(TAG, "loadJsonArrayFromNetwork : exit");
        return jsonArray;
    }

//    private void saveImage( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
//        Log.v( TAG, "saveImage : enter, url=" +job.getUrl() + ", filename=" + job.getFilename() );
//
//        if( null == job.getUrl() ) {
//            Log.v( TAG, "saveImage : exit, url is null" );
//
//            return;
//        }
//
//        if( null == job.getFilename() ) {
//            Log.v( TAG, "saveImage : exit, filename is null" );
//
//            return;
//        }
//
//        Bitmap bitmap = loadBitmapFromNetwork( job );
//
//        FileOutputStream fos = mContext.openFileOutput( job.getFilename(), Context.MODE_PRIVATE );
//        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, fos );
//        fos.close();
//
//        if( null != job.getId() ) {
//
//            DateTime lastRun = new DateTime( DateTimeZone.UTC );
//            ContentValues update = new ContentValues();
//            update.put( WorkItem._ID, job.getId() );
//            update.put( WorkItem.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );
//            update.put( WorkItem.FIELD_ETAG, job.getEtag() );
//            update.put( WorkItem.FIELD_LAST_RUN, lastRun.getMillis() );
//            update.put( WorkItem.FIELD_STATUS, job.getStatus().name() );
//
//            provider.update( ContentUris.withAppendedId( WorkItem.CONTENT_URI, job.getId() ), update, null, null );
//
//        }
//
//        Log.v( TAG, "saveImage : exit" );
//    }

    private Bitmap loadBitmapFromNetwork( Job job ) throws IOException {
        Log.v( TAG, "loadBitmapFromNetwork : enter" );

        Bitmap bitmap = null;
        InputStream stream = null;

        try {

            stream = downloadUrl( job );

            bitmap = BitmapFactory.decodeStream( stream );

            job.setStatus( WorkItem.Status.OK );

        } finally {

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            if( null != stream  ) {
                stream.close();
            }

        }

        Log.v( TAG, "loadBitmapFromNetwork : exit" );
        return bitmap;
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl( Job job ) throws IOException {
        Log.v( TAG, "downloadUrl : enter, url=" + job.getUrl() );

        long currentTime = System.currentTimeMillis();
        InputStream stream = null;

//        TrafficStats.setThreadStatsTag( 0xF00D );
//        try {
            URL url = new URL( job.getUrl() );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout( 10000 /* milliseconds */ );
            conn.setConnectTimeout( 15000 /* milliseconds */ );
            conn.setRequestMethod( "GET" );

            if( null != job.getEtag() && !"".equals( job.getEtag() ) ) {
                conn.setRequestProperty( "If-None-Match", job.getEtag() );
            }

            conn.setDoInput( true );

            // Starts the query
            conn.connect();

            if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                Log.v( TAG, "downloadUrl : HTTP OK" );

                job.setEtag( conn.getHeaderField( "ETag" ) );

                stream = conn.getInputStream();

            } else if( conn.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED ) {
                Log.v( TAG, "downloadUrl : HTTP NOT MODIFIED" );

                job.setStatus( WorkItem.Status.NOT_MODIFIED );
            }
//        } finally {
//            TrafficStats.clearThreadStatsTag();
//        }

        Log.v( TAG, "downloadUrl : exit" );
        return stream;
    }

    private void enableHttpResponseCache() {
        try {
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            File httpCacheDir = new File( mContext.getCacheDir(), "http" );
            Class.forName( "android.net.http.HttpResponseCache" )
                    .getMethod( "install", File.class, long.class )
                    .invoke( null, httpCacheDir, httpCacheSize );
        } catch( Exception httpResponseCacheNotAvailable ) {
            Log.d(TAG, "HTTP response cache is unavailable.");
        }
    }

    private void processShows( JSONArray jsonArray, ContentProviderClient provider, Job job ) throws RemoteException {
        Log.v( TAG, "processShows : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItem._ID, job.getId() );
        update.put( WorkItem.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {
            int count = 0, loaded = 0;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { Show._ID };

            ContentValues values;

            for( int i = 0; i < jsonArray.length(); i++ ) {
                JSONObject json = jsonArray.getJSONObject( i );
                Log.v( TAG, "processShows : json=" + json.toString() );

                int showNameId = json.getInt( "ShowNameId" );

                int vip = 0;
                try {
                    vip = json.getBoolean( "VIP" ) ? 1 : 0;
                } catch( Exception e ) {
                    Log.v( TAG, "processShows : VIP format is not valid" );
                }

                int sortOrder = 0;
                try {
                    sortOrder = json.getInt( "SortOrder" );
                } catch( Exception e ) {
                    Log.v( TAG, "processShows : SortOrder format is not valid" );
                }

                String name = json.getString( "Name" );
                String prefix = json.getString( "Prefix" );
                String coverImageUrl = json.getString( "CoverImageUrl" );

                values = new ContentValues();
                values.put( Show._ID, showNameId );
                values.put( Show.FIELD_NAME, name );
                values.put( Show.FIELD_PREFIX, prefix );
                values.put( Show.FIELD_VIP, vip );
                values.put( Show.FIELD_SORTORDER, sortOrder );
                values.put( Show.FIELD_DESCRIPTION, json.getString( "Description" ) );
                values.put( Show.FIELD_COVERIMAGEURL, coverImageUrl );
                values.put( Show.FIELD_FORUMURL, json.getString( "ForumUrl" ) );
                values.put( Show.FIELD_PREVIEWURL, json.getString( "PreviewUrl" ) );
                values.put( Show.FIELD_EPISODE_COUNT, json.getInt( "EpisodeCount" ) );
                values.put( Show.FIELD_EPISODE_COUNT_MAX, json.getInt( "EpisodeNumberMax" ) );
                values.put( Show.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                Cursor cursor = provider.query( ContentUris.withAppendedId( Show.CONTENT_URI, showNameId ), projection, null, null, null );
                if( cursor.moveToFirst() ) {
                    Log.v( TAG, "processShows : show iteration, updating existing entry" );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( Show._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( Show.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                } else {
                    Log.v( TAG, "processShows : show iteration, adding new entry" );

                    ops.add(
                            ContentProviderOperation.newInsert( Show.CONTENT_URI )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                }
                cursor.close();
                count++;

//                values = new ContentValues();
//                values.put( WorkItem.FIELD_NAME, name + " cover" );
//                values.put( WorkItem.FIELD_FREQUENCY, WorkItem.Frequency.WEEKLY.name() );
//                values.put( WorkItem.FIELD_DOWNLOAD, WorkItem.Download.JPG.name() );
//                values.put( WorkItem.FIELD_ENDPOINT, Endpoint.Type.IMAGE.name() );
//                values.put( WorkItem.FIELD_ADDRESS, coverImageUrl );
//                values.put( WorkItem.FIELD_PARAMETERS, prefix + "_cover.jpg" );
//                values.put( WorkItem.FIELD_STATUS, WorkItem.Status.NEVER.name() );
//                values.put( WorkItem.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );
//
//                cursor = provider.query( WorkItem.CONTENT_URI, null, WorkItem.FIELD_ENDPOINT + " = ?", new String[] { coverImageUrl }, null );
//                if( cursor.moveToNext() ) {
//                    Log.v( TAG, "processEpisodes : guest image small, updating existing entry" );
//
//                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( WorkItem._ID ) );
//                    ops.add(
//                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( WorkItem.CONTENT_URI, id ) )
//                                    .withValues( values )
//                                    .withYieldAllowed( true )
//                                    .build()
//                    );
//
//                } else {
//                    Log.v( TAG, "processEpisodes : guest image small, adding new entry" );
//
//                    ops.add(
//                            ContentProviderOperation.newInsert( WorkItem.CONTENT_URI )
//                                    .withValues( values )
//                                    .withYieldAllowed( true )
//                                    .build()
//                    );
//
//                    Job coverJob = new Job();
//                    coverJob.setUrl( coverImageUrl );
//                    coverJob.setFilename( prefix + "_cover.jpg" );
//
//                    saveImage( provider, coverJob );
//                }
//                cursor.close();
//                count++;

                if( showNameId ==  1 ) {
                    Log.v( TAG, "processShows : adding on time update for katg main show" );

                    values = new ContentValues();
                    values.put( WorkItem.FIELD_NAME, "Refresh " + json.getString( "Name" ) );
                    values.put( WorkItem.FIELD_FREQUENCY, WorkItem.Frequency.ONCE.name() );
                    values.put( WorkItem.FIELD_DOWNLOAD, WorkItem.Download.JSONARRAY.name() );
                    values.put( WorkItem.FIELD_ENDPOINT, Endpoint.Type.LIST.name() );
                    values.put( WorkItem.FIELD_ADDRESS, Endpoint.LIST );
                    values.put( WorkItem.FIELD_PARAMETERS, "?shownameid=" + showNameId );
                    values.put( WorkItem.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                    cursor = provider.query( WorkItem.CONTENT_URI, null, WorkItem.FIELD_ENDPOINT + " = ? and " + WorkItem.FIELD_PARAMETERS + " = ?", new String[] { Endpoint.LIST, "?shownameid=" + showNameId }, null );
                    if( cursor.moveToNext() ) {
                        Log.v( TAG, "processShows : updating daily show" );

                        values.put( WorkItem.FIELD_LAST_RUN, new DateTime( DateTimeZone.UTC ).getMillis() );

                        Long id = cursor.getLong( cursor.getColumnIndexOrThrow( WorkItem._ID ) );
                        ops.add(
                                ContentProviderOperation.newUpdate( ContentUris.withAppendedId( WorkItem.CONTENT_URI, id ) )
                                        .withValues( values )
                                        .withYieldAllowed( true )
                                        .build()
                        );

                    } else {
                        Log.v( TAG, "processShows : adding daily show" );

                        values.put( WorkItem.FIELD_LAST_RUN, -1 );
                        values.put( WorkItem.FIELD_STATUS, WorkItem.Status.NEVER.name() );

                        ops.add(
                                ContentProviderOperation.newInsert( WorkItem.CONTENT_URI )
                                        .withValues( values )
                                        .withYieldAllowed( true )
                                        .build()
                        );
                    }
                    count++;

                }

                if( showNameId >  1 ) {
                    Log.v( TAG, "processShows : adding daily updates for spinoff shows" );

                    values = new ContentValues();
                    values.put( WorkItem.FIELD_NAME, "Refresh " + json.getString( "Name" ) );
                    values.put( WorkItem.FIELD_FREQUENCY, WorkItem.Frequency.DAILY.name() );
                    values.put( WorkItem.FIELD_DOWNLOAD, WorkItem.Download.JSONARRAY.name() );
                    values.put( WorkItem.FIELD_ENDPOINT, Endpoint.Type.LIST.name() );
                    values.put( WorkItem.FIELD_ADDRESS, Endpoint.LIST );
                    values.put( WorkItem.FIELD_PARAMETERS, "?shownameid=" + showNameId );
                    values.put( WorkItem.FIELD_STATUS, WorkItem.Status.NEVER.name() );
                    values.put( WorkItem.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                    cursor = provider.query( WorkItem.CONTENT_URI, null, WorkItem.FIELD_ENDPOINT + " = ? and " + WorkItem.FIELD_PARAMETERS + " = ?", new String[] { Endpoint.LIST, "?shownameid=" + showNameId }, null );
                    if( cursor.moveToNext() ) {
                        Log.v( TAG, "processShows : updating daily spinoff show" );

                        values.put( WorkItem.FIELD_LAST_RUN, new DateTime( DateTimeZone.UTC ).getMillis() );

                        Long id = cursor.getLong( cursor.getColumnIndexOrThrow( WorkItem._ID ) );
                        ops.add(
                                ContentProviderOperation.newUpdate( ContentUris.withAppendedId( WorkItem.CONTENT_URI, id ) )
                                        .withValues( values )
                                        .withYieldAllowed( true )
                                        .build()
                        );

                    } else {
                        Log.v( TAG, "processShows : adding daily spinoff show" );

                        values.put( WorkItem.FIELD_LAST_RUN, -1 );
                        values.put( WorkItem.FIELD_STATUS, WorkItem.Status.NEVER.name() );

                        ops.add(
                                ContentProviderOperation.newInsert( WorkItem.CONTENT_URI )
                                        .withValues( values )
                                        .withYieldAllowed( true )
                                        .build()
                        );
                    }
                    count++;

                }

                if( count > 100 ) {
                    Log.v( TAG, "processShows : applying batch for '" + count + "' transactions" );

                    if( !ops.isEmpty() ) {

                        ContentProviderResult[] results = provider.applyBatch( ops );
                        loaded += results.length;

                        if( results.length > 0 ) {
                            ops.clear();
                        }
                    }

                    count = 0;
                }
            }

            if( !ops.isEmpty() ) {
                Log.v( TAG, "processShows : applying final batch for '" + count + "' transactions" );

                ContentProviderResult[] results = provider.applyBatch( ops );
                loaded += results.length;

                if( results.length > 0 ) {
                    ops.clear();
                }
            }

            Log.i( TAG, "processShows : shows loaded '" + loaded + "'" );

            update.put( WorkItem.FIELD_ETAG, job.getEtag() );
            update.put( WorkItem.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItem.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e( TAG, "processShows : error", e );

            update.put( WorkItem.FIELD_STATUS, WorkItem.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItem.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "processShows : exit" );
    }

    private void processEvents( JSONObject jsonObject, ContentProviderClient provider, Job job ) throws RemoteException {
        Log.v( TAG, "processEvents : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItem._ID, job.getId() );
        update.put( WorkItem.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {
            int count = 0, loaded = 0;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { Event._ID };

            ContentValues values;

            for( int i = 0; i < jsonObject.getJSONArray( "events" ).length(); i++ ) {
                JSONObject json = jsonObject.getJSONArray( "events" ).getJSONObject(i);
                Log.v( TAG, "processEvents : json=" + json.toString() );

                String eventId = json.getString( "eventid" );

                DateTime startDate = new DateTime();
                try {
                    startDate = format.parseDateTime( json.getString( "startdate" ) );
                } catch( JSONException e ) {
                    Log.v( TAG, "processEvents : startdate is not valid" );
                }
                startDate = startDate.withZone( DateTimeZone.UTC );

                DateTime endDate = new DateTime();
                try {
                    endDate = format.parseDateTime( json.getString( "enddate" ) );
                } catch( JSONException e ) {
                    Log.v( TAG, "processEvents : enddate is not valid" );
                }
                endDate = endDate.withZone( DateTimeZone.UTC );

                Log.v(TAG, "processEvents : startDate=" + startDate.toString() + ", endDate=" + endDate.toString());

                values = new ContentValues();
                values.put( Event.FIELD_EVENTID, eventId );
                values.put( Event.FIELD_TITLE, json.getString( "title" ) );
                values.put( Event.FIELD_LOCATION, json.getString( "location" ) );
                values.put( Event.FIELD_STARTDATE, startDate.getMillis() );
                values.put( Event.FIELD_ENDDATE, endDate.getMillis() );
                values.put( Event.FIELD_DETAILS, json.getString( "details" ) );
                values.put( Event.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                Cursor cursor = provider.query( Event.CONTENT_URI, projection, Event.FIELD_EVENTID + "=?", new String[] { eventId }, null );
                if( cursor.moveToFirst() ) {
                    Log.v( TAG, "processEvents : show iteration, updating existing entry" );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( Event._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( Event.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                } else {
                    Log.v( TAG, "processEvents : show iteration, adding new entry" );

                    ops.add(
                            ContentProviderOperation.newInsert( Event.CONTENT_URI )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                }
                cursor.close();
                count++;

                if( count > 100 ) {
                    Log.v( TAG, "processEvents : applying batch for '" + count + "' transactions" );

                    if( !ops.isEmpty() ) {

                        ContentProviderResult[] results = provider.applyBatch( ops );
                        loaded += results.length;

                        if( results.length > 0 ) {
                            ops.clear();
                        }
                    }

                    count = 0;
                }
            }

            if( !ops.isEmpty() ) {
                Log.v( TAG, "processEvents : applying final batch for '" + count + "' transactions" );

                ContentProviderResult[] results = provider.applyBatch( ops );
                loaded += results.length;

                if( results.length > 0 ) {
                    ops.clear();
                }
            }

            Log.i( TAG, "processEvents : events loaded '" + loaded + "'" );

            update.put( WorkItem.FIELD_ETAG, job.getEtag() );
            update.put( WorkItem.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItem.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e( TAG, "processEvents : error", e );

            update.put( WorkItem.FIELD_STATUS, WorkItem.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItem.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v(TAG, "processEvents : exit");
    }

    private void processLives( JSONObject jsonObject, ContentProviderClient provider, Job job ) throws RemoteException {
        Log.v( TAG, "processLives : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItem._ID, job.getId() );
        update.put(WorkItem.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis());

        int broadcasting = 0;
        try {
            broadcasting = jsonObject.getBoolean( "broadcasting" ) ? 1 : 0;

            ContentValues values = new ContentValues();
            values.put( Live.FIELD_BROADCASTING, broadcasting );
            values.put( Live.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

            provider.update( ContentUris.withAppendedId( Live.CONTENT_URI, 1 ), values, null, null );

            update.put( WorkItem.FIELD_ETAG, job.getEtag() );
            update.put( WorkItem.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItem.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.v( TAG, "processLives : broadcasting format is not valid" );

            update.put( WorkItem.FIELD_STATUS, WorkItem.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItem.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "processLives : exit" );
    }

    private void processEpisodes( JSONArray jsonArray, ContentProviderClient provider, Endpoint.Type type ) {
        Log.v( TAG, "processEpisodes : enter" );

        try {
            int loaded = 0;
            List<Integer> detailsQueue = new ArrayList<Integer>();

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { Episode._ID, Episode.FIELD_DOWNLOADED, Episode.FIELD_PLAYED, Episode.FIELD_LASTPLAYED };

            ContentValues values;

            for( int i = 0; i < jsonArray.length(); i++ ) {
                JSONObject json = jsonArray.getJSONObject( i );
                Log.v( TAG, "processEpisodes : json=" + json.toString() );

                int showId = json.getInt( "ShowId" );
                detailsQueue.add( showId );

                DateTime posted = new DateTime();
                try {
                    posted = formata.parseDateTime( json.getString( "PostedDate" ) );
                } catch( Exception e ) {
                    Log.v( TAG, "processEpisodes : PostedDate format is not valid" );
                }
                posted = posted.withZone( DateTimeZone.UTC );

                String previewUrl = "";
                try {
                    previewUrl = json.getString( "PreviewUrl" );
                } catch( JSONException e ) {
                    Log.v( TAG, "processEpisodes : PreviewUrl format is not valid or does not exist" );
                }

                String fileUrl = "", fileName = "";
                try {
                    fileUrl = json.getString( "FileUrl" );

                    Uri uri = Uri.parse( fileUrl );
                    if( null != uri.getLastPathSegment() ) {
                        fileName = uri.getLastPathSegment();
                        Log.v( TAG, "processEpisodes : fileName=" + fileName );
                    }
                } catch( JSONException e ) {
                    Log.v( TAG, "processEpisodes : FileUrl format is not valid or does not exist" );
                }

                int length = -1;
                try {
                    length = json.getInt("Length");
                } catch( Exception e ) {
                    Log.v( TAG, "processEpisodes : Length format is not valid or not present" );
                }

                int fileSize = -1;
                try {
                    fileSize = json.getInt( "FileSize" );
                } catch( Exception e ) {
                    Log.v( TAG, "processEpisodes : FileSize format is not valid or not present" );
                }

                int vip = 0;
                try {
                    vip = json.getInt( "public" );
                } catch( Exception e ) {
                    Log.v( TAG, "processEpisodes : Public format is not valid or not present" );
                }

                int showNameId = -1;
                try {
                    showNameId = json.getInt( "ShowNameId" );
                } catch( Exception e ) {
                    Log.v( TAG, "processEpisodes : ShowNameId format is not valid or not present" );
                }

                values = new ContentValues();
                values.put( Episode._ID, showId );
                values.put( Episode.FIELD_NUMBER, json.getInt( "Number" ) );
                values.put( Episode.FIELD_TITLE, json.getString("Title") );
                values.put( Episode.FIELD_PREVIEWURL, previewUrl );
                values.put( Episode.FIELD_FILEURL, fileUrl );
                values.put( Episode.FIELD_FILENAME, fileName );
                values.put( Episode.FIELD_LENGTH, length );
                values.put( Episode.FIELD_FILESIZE, fileSize );
                values.put( Episode.FIELD_TYPE, json.getInt( "Type" ) );
                values.put( Episode.FIELD_PUBLIC, vip );
                values.put( Episode.FIELD_TIMESTAMP, posted.getMillis() );
                values.put( Episode.FIELD_SHOWNAMEID, showNameId );
                values.put( Episode.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                Cursor cursor = provider.query( ContentUris.withAppendedId( Episode.CONTENT_URI, showId ), projection, null, null, null );
                if( cursor.moveToFirst() ) {
                    Log.v( TAG, "processEpisodes : episode iteration, updating existing entry" );

                    long downloaded = cursor.getLong( cursor.getColumnIndex( Episode.FIELD_DOWNLOADED ) );
                    long played = cursor.getLong( cursor.getColumnIndex( Episode.FIELD_PLAYED ) );
                    long lastplayed = cursor.getLong( cursor.getColumnIndex( Episode.FIELD_LASTPLAYED ) );

                    values.put( Episode.FIELD_DOWNLOADED, downloaded );
                    values.put( Episode.FIELD_PLAYED, played );
                    values.put( Episode.FIELD_LASTPLAYED, lastplayed );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( Episode._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( Episode.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                } else {
                    Log.v( TAG, "processEpisodes : episode iteration, adding new entry" );

                    values.put( Episode.FIELD_DOWNLOADED, -1 );
                    values.put( Episode.FIELD_PLAYED, -1 );
                    values.put( Episode.FIELD_LASTPLAYED, -1 );

                    ops.add(
                            ContentProviderOperation.newInsert( Episode.CONTENT_URI )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                }
                cursor.close();

                Log.v( TAG, "processEpisodes : processing guests" );
                JSONArray guests = json.getJSONArray( "Guests" );
                if( guests.length() > 0 ) {

                    for( int j = 0; j < guests.length(); j++ ) {

                        JSONObject guest = guests.getJSONObject( j );
                        Log.v( TAG, "processEpisodes : guest=" + guest.toString() );

                        int showGuestId = guest.getInt( "ShowGuestId" );
                        String name = guest.getString( "RealName" );
                        String pictureUrl = guest.getString( "PictureUrl" );
                        String pictureUrlLarge = guest.getString( "PictureUrlLarge" );

                        values = new ContentValues();
                        values.put( Guest._ID, showGuestId );
                        values.put( Guest.FIELD_REALNAME, name );
                        values.put( Guest.FIELD_DESCRIPTION, guest.getString( "Description" ) );
                        values.put( Guest.FIELD_PICTUREFILENAME, guest.getString( "PictureFilename" ) );
                        values.put( Guest.FIELD_URL1, guest.getString( "Url1" ) );
                        values.put( Guest.FIELD_URL2, guest.getString( "Url2" ) );
                        values.put( Guest.FIELD_PICTUREURL, pictureUrl );
                        values.put( Guest.FIELD_PICTUREURLLARGE, pictureUrlLarge );
                        values.put( Guest.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                        cursor = provider.query( ContentUris.withAppendedId( Guest.CONTENT_URI, showGuestId ), null, null, null, null );
                        if( cursor.moveToFirst() ) {
                            Log.v( TAG, "processEpisodes : guest iteration, updating existing entry" );

                            Long id = cursor.getLong(cursor.getColumnIndexOrThrow( Guest._ID ) );
                            ops.add(
                                    ContentProviderOperation.newUpdate( ContentUris.withAppendedId( Guest.CONTENT_URI, id ) )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );

                        } else {
                            Log.v( TAG, "processEpisodes : guest iteration, adding new entry" );

                            ops.add(
                                    ContentProviderOperation.newInsert( Guest.CONTENT_URI )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );

                        }
                        cursor.close();

                        values = new ContentValues();
                        values.put( EpisodeGuests.FIELD_SHOWID, showId );
                        values.put( EpisodeGuests.FIELD_SHOWGUESTID, showGuestId );
                        values.put( Guest.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                        cursor = provider.query( EpisodeGuests.CONTENT_URI, null, EpisodeGuests.FIELD_SHOWID + "=? and " + EpisodeGuests.FIELD_SHOWGUESTID + "=?", new String[] { String.valueOf( showId ), String.valueOf( showGuestId ) }, null );
                        if( cursor.moveToFirst() ) {
                            Log.v( TAG, "processEpisodes : episodeGuest iteration, updating existing entry" );

                            Long id = cursor.getLong( cursor.getColumnIndexOrThrow( EpisodeGuests._ID ) );
                            ops.add(
                                    ContentProviderOperation.newUpdate( ContentUris.withAppendedId( EpisodeGuests.CONTENT_URI, id ) )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );

                        } else {
                            Log.v( TAG, "processEpisodes : episodeGuest iteration, adding new entry" );

                            ops.add(
                                    ContentProviderOperation.newInsert( EpisodeGuests.CONTENT_URI )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );

                        }
                        cursor.close();

//                        if( !"".equals( pictureUrl ) ) {
//
//                            Uri imageUri = Uri.parse( pictureUrl );
//
//                            values = new ContentValues();
//                            values.put( WorkItem.FIELD_NAME, name + " small" );
//                            values.put( WorkItem.FIELD_FREQUENCY, WorkItem.Frequency.WEEKLY.name() );
//                            values.put( WorkItem.FIELD_DOWNLOAD, WorkItem.Download.JPG.name() );
//                            values.put( WorkItem.FIELD_ENDPOINT, Endpoint.Type.IMAGE.name() );
//                            values.put( WorkItem.FIELD_ADDRESS, pictureUrl );
//                            values.put( WorkItem.FIELD_PARAMETERS, imageUri.getLastPathSegment() );
//                            values.put( WorkItem.FIELD_STATUS, WorkItem.Status.NEVER.name() );
//                            values.put( WorkItem.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );
//
//                            cursor = provider.query( WorkItem.CONTENT_URI, null, WorkItem.FIELD_DOWNLOAD + "= ? AND " + WorkItem.FIELD_NAME + " = ?", new String[] { WorkItem.Download.JPG.name(), name + " small" }, null );
//                            if( cursor.moveToNext() ) {
//                                Log.v( TAG, "processEpisodes : guest image small, updating existing entry" );
//
//                                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( WorkItem._ID ) );
//                                ops.add(
//                                        ContentProviderOperation.newUpdate( ContentUris.withAppendedId( WorkItem.CONTENT_URI, id ) )
//                                                .withValues( values )
//                                                .withYieldAllowed( true )
//                                                .build()
//                                );
//
//                            } else {
//                                Log.v( TAG, "processEpisodes : guest image small, adding new entry" );
//
//                                ops.add(
//                                        ContentProviderOperation.newInsert( WorkItem.CONTENT_URI )
//                                                .withValues( values )
//                                                .withYieldAllowed( true )
//                                                .build()
//                                );
//
//                                Job job = new Job();
//                                job.setUrl( pictureUrl );
//                                job.setFilename( imageUri.getLastPathSegment()) ;
//
//                                saveImage( provider, job );
//
//                            }
//                            cursor.close();
//
//                        }

//                        if( !"".equals( pictureUrlLarge ) ) {
//
//                            Uri imageUri = Uri.parse( pictureUrlLarge );
//
//                            values = new ContentValues();
//                            values.put( WorkItem.FIELD_NAME, name + " large" );
//                            values.put( WorkItem.FIELD_FREQUENCY, WorkItem.Frequency.WEEKLY.name() );
//                            values.put( WorkItem.FIELD_DOWNLOAD, WorkItem.Download.JPG.name() );
//                            values.put( WorkItem.FIELD_ENDPOINT, Endpoint.Type.IMAGE.name() );
//                            values.put( WorkItem.FIELD_ADDRESS, pictureUrlLarge );
//                            values.put( WorkItem.FIELD_PARAMETERS, imageUri.getLastPathSegment() );
//                            values.put( WorkItem.FIELD_STATUS, WorkItem.Status.NEVER.name() );
//                            values.put( WorkItem.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );
//
//                            cursor = provider.query( WorkItem.CONTENT_URI, null, WorkItem.FIELD_ENDPOINT + " = ?", new String[] { pictureUrl }, null );
//                            if( cursor.moveToNext() ) {
//                                Log.v( TAG, "processEpisodes : guest image large, updating existing entry" );
//
//                                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( WorkItem._ID ) );
//                                ops.add(
//                                        ContentProviderOperation.newUpdate( ContentUris.withAppendedId( WorkItem.CONTENT_URI, id ) )
//                                                .withValues( values )
//                                                .withYieldAllowed( true )
//                                                .build()
//                                );
//
//                            } else {
//                                Log.v( TAG, "processEpisodes : guest image large, adding new entry" );
//
//                                ops.add(
//                                        ContentProviderOperation.newInsert( WorkItem.CONTENT_URI )
//                                                .withValues( values )
//                                                .withYieldAllowed( true )
//                                                .build()
//                                );
//
//                                Job job = new Job();
//                                job.setUrl( pictureUrlLarge );
//                                job.setFilename( imageUri.getLastPathSegment()) ;
//
//                                saveImage( provider, job );
//
//                            }
//                            cursor.close();
//
//                        }

                    }
                }

                if( !ops.isEmpty() ) {

                    ContentProviderResult[] results = provider.applyBatch( ops );
                    loaded += results.length;

                    if( results.length > 0 ) {
                        ops.clear();
                    }
                }

            }

            if( !ops.isEmpty() ) {
                Log.v( TAG, "processEpisodes : applying final batch for transactions" );

                ContentProviderResult[] results = provider.applyBatch( ops );
                loaded += results.length;

                if( results.length > 0 ) {
                    ops.clear();
                }
            }

            if( Endpoint.Type.RECENT.equals( type ) ) {
                if( !detailsQueue.isEmpty() ) {
                    Log.v( TAG, "processEpisodes : processing show details" );

                    for( int showId : detailsQueue ) {
                        getShowDetails( provider, showId );
                    }
                }
            }

            Log.i( TAG, "processEpisodes : episodes loaded '" + loaded + "'" );

        } catch( Exception e ) {
            Log.e( TAG, "processEpisodes : error", e );
        }

        Log.v( TAG, "processEpisodes : exit" );
    }

    private void processEpisodeDetails( JSONObject jsonObject, ContentProviderClient provider, int showId ) {
        Log.v( TAG, "processEpisodeDetails : enter" );

        try {
            int count = 0, loaded = 0;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { Detail._ID };

            ContentValues values = new ContentValues();
            values.put( Detail.FIELD_NOTES, jsonObject.getString( "notes" ) );
            values.put( Detail.FIELD_FORUMURL, jsonObject.getString( "forum_url" ) );
            values.put( Detail.FIELD_SHOWID, showId );
            values.put( Detail.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

            Cursor cursor = provider.query( Detail.CONTENT_URI, projection, Detail.FIELD_SHOWID + "=?", new String[] { String.valueOf( showId ) }, null );
            if( cursor.moveToFirst() ) {
                Log.v( TAG, "processEpisodeDetails : detail iteration, updating existing entry" );

                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( Detail._ID ) );
                ops.add(
                        ContentProviderOperation.newUpdate( ContentUris.withAppendedId( Detail.CONTENT_URI, id ) )
                                .withValues( values )
                                .withYieldAllowed( true )
                                .build()
                );

            } else {
                Log.v( TAG, "processEpisodeDetails : detail iteration, adding new entry" );

                ops.add(
                        ContentProviderOperation.newInsert( Detail.CONTENT_URI )
                                .withValues( values )
                                .withYieldAllowed( true )
                                .build()
                );

            }
            cursor.close();

            for( int i = 0; i < jsonObject.getJSONArray( "images" ).length(); i++ ) {
                JSONObject json = jsonObject.getJSONArray( "images" ).getJSONObject( i );
                Log.v( TAG, "processEpisodeDetails : json=" + json.toString() );

                int pictureid = json.getInt( "pictureid" );

                int explicit = 0;
                try {
                    explicit = json.getInt( "explicit" );
                } catch( Exception e ) {
                    Log.v( TAG, "processEpisodes : Public format is not valid or not present" );
                }

                values = new ContentValues();
                values.put( Image._ID, pictureid );
                values.put( Image.FIELD_TITLE, json.getString( "title" ) );
                values.put( Image.FIELD_DESCRIPTION, json.getString( "description" ) );
                values.put( Image.FIELD_EXPLICIT, explicit );
                values.put( Image.FIELD_DISPLAY_ORDER, json.getInt( "displayorder" ) );
                values.put( Image.FIELD_MEDIAURL, json.getString( "media_url" ) );
                values.put( Image.FIELD_SHOWID, showId );
                values.put( Image.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                cursor = provider.query( ContentUris.withAppendedId( Image.CONTENT_URI, pictureid ), null, null, null, null );
                if( cursor.moveToFirst() ) {
                    Log.v( TAG, "processEpisodeDetails : image iteration, updating existing entry" );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( Image._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( Image.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                } else {
                    Log.v( TAG, "processEpisodeDetails : image iteration, adding new entry" );

                    ops.add(
                            ContentProviderOperation.newInsert( Image.CONTENT_URI )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                }
                cursor.close();
                count++;

                if( count > 100 ) {
                    Log.v( TAG, "processEpisodeDetails : applying batch for '" + count + "' transactions" );

                    if( !ops.isEmpty() ) {

                        ContentProviderResult[] results = provider.applyBatch( ops );
                        loaded += results.length;

                        if( results.length > 0 ) {
                            ops.clear();
                        }
                    }

                    count = 0;
                }
            }

            if( !ops.isEmpty() ) {
                Log.v( TAG, "processEpisodeDetails : applying final batch for '" + count + "' transactions" );

                ContentProviderResult[] results = provider.applyBatch( ops );
                loaded += results.length;

                if( results.length > 0 ) {
                    ops.clear();
                }
            }

            Log.i( TAG, "processEpisodeDetails : details loaded '" + loaded + "'" );

        } catch( Exception e ) {
            Log.e( TAG, "processEpisodeDetails : error", e );
        }

        Log.v( TAG, "processEpisodeDetails : exit" );
    }

    private class Job {

        private Long id;
        private Endpoint.Type type;
        private WorkItem.Download download;
        private String url;
        private String filename;
        private String etag;
        private WorkItem.Status status;

        public Long getId() { return id; }

        public void setId( Long id ) { this.id = id; }

        public WorkItem.Download getDownload() { return download; }

        public void setDownload( WorkItem.Download download ) { this.download = download; }

        public Endpoint.Type getType() {
            return type;
        }

        public void setType( Endpoint.Type type ) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl( String url ) {
            this.url = url;
        }

        public String getFilename() { return filename; }

        public void setFilename( String filename ) { this.filename = filename; }

        public WorkItem.Status getStatus() {
            return status;
        }

        public void setStatus( WorkItem.Status status ) {
            this.status = status;
        }

        public String getEtag() {
            return etag;
        }

        public void setEtag( String etag ) {
            this.etag = etag;
        }

    }

}
