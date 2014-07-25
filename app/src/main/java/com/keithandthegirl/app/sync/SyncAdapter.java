package com.keithandthegirl.app.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.keithandthegirl.app.db.model.DetailConstants;
import com.keithandthegirl.app.db.model.EndpointConstants;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.EpisodeGuestConstants;
import com.keithandthegirl.app.db.model.EventConstants;
import com.keithandthegirl.app.db.model.GuestConstants;
import com.keithandthegirl.app.db.model.ImageConstants;
import com.keithandthegirl.app.db.model.LiveConstants;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.db.model.WorkItemConstants;
import com.keithandthegirl.app.db.model.YoutubeConstants;

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

    public static final String START_ACTION = "com.keithandthegirl.app.sync.START_ACTION";
    public static final String COMPLETE_ACTION = "com.keithandthegirl.app.sync.COMPLETE_ACTION";

    private static final DateTimeFormatter format = DateTimeFormat.forPattern( "MM/dd/yyyy HH:mm" ).withZone( DateTimeZone.forTimeZone( TimeZone.getTimeZone( "America/New_York" ) ) );
    private static final DateTimeFormatter formata = DateTimeFormat.forPattern( "M/d/yyyy hh:mm:ss a" ).withZone( DateTimeZone.forTimeZone( TimeZone.getTimeZone( "America/New_York" ) ) );
    private static final DateTimeFormatter formaty = DateTimeFormat.forPattern( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" ).withZone( DateTimeZone.forTimeZone( TimeZone.getTimeZone( "America/New_York" ) ) );

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
        Log.i( TAG, "onPerformSync : enter" );

        /*
         * Put the data transfer code here.
         */
        SyncResult result = new SyncResult();
        try {
            Intent startIntent = new Intent();
            startIntent.setAction( START_ACTION );
            mContext.sendBroadcast( startIntent );

            DateTime now = new DateTime( DateTimeZone.UTC );

            List<Job> jobs = new ArrayList<Job>();
            Cursor cursor = provider.query( WorkItemConstants.CONTENT_URI, null, null, null, null );
            while( cursor.moveToNext() ) {
                Job job = new Job();

                Long id = cursor.getLong( cursor.getColumnIndex( WorkItemConstants._ID ) );
                job.setId( id );

                WorkItemConstants.Frequency wtype = WorkItemConstants.Frequency.valueOf( cursor.getString( cursor.getColumnIndex( WorkItemConstants.FIELD_FREQUENCY ) ) );

                WorkItemConstants.Download dtype = WorkItemConstants.Download.valueOf( cursor.getString( cursor.getColumnIndex( WorkItemConstants.FIELD_DOWNLOAD ) ) );
                job.setDownload( dtype );

                EndpointConstants.Type type = EndpointConstants.Type.valueOf( cursor.getString( cursor.getColumnIndex( WorkItemConstants.FIELD_ENDPOINT ) ) );
                job.setType( type );

                String address = cursor.getString( cursor.getColumnIndex( WorkItemConstants.FIELD_ADDRESS ) );
                String parameters = cursor.getString( cursor.getColumnIndex( WorkItemConstants.FIELD_PARAMETERS ) );
                if( dtype.equals( WorkItemConstants.Download.JPG ) ) {
                    job.setUrl( address );
                    job.setFilename( parameters );
                } else {
                    job.setUrl( address + parameters );
                }

                WorkItemConstants.Status status = WorkItemConstants.Status.valueOf( cursor.getString( cursor.getColumnIndex( WorkItemConstants.FIELD_STATUS ) ) );
                job.setStatus( status );

                DateTime lastRun = new DateTime( DateTimeZone.UTC );
                long lastRunMs = cursor.getLong( cursor.getColumnIndex( WorkItemConstants.FIELD_LAST_RUN ) );
                if( lastRunMs > 0 ) {
                    lastRun = new DateTime( lastRunMs );
                }

                Log.i( TAG, "onPerformSync : job=" + job.toString() );

                switch( wtype ) {
                    case ON_DEMAND:
                        if( !status.equals( WorkItemConstants.Status.OK ) ) {
                            Log.v( TAG, "onPerformSync : adding On Demand job" );

                            jobs.add( job );
                        }
                        break;
                    case ONCE:
                        if( !status.equals( WorkItemConstants.Status.OK ) ) {
                            Log.v( TAG, "onPerformSync : adding One Time job" );

                            jobs.add( job );
                        }
                        break;
                    case HOURLY:
                        if( status.equals( WorkItemConstants.Status.NEVER ) ) {
                            Log.v( TAG, "onPerformSync : adding Hourly job, never run" );

                            jobs.add( job );
                        } else {
                            if( Minutes.minutesBetween( lastRun, now ).getMinutes() >= 60 ) {
                                Log.v( TAG, "onPerformSync : adding Hourly job" );

                                jobs.add( job );
                            }
                        }
                        break;
                    case DAILY:
                        if( status.equals( WorkItemConstants.Status.NEVER ) ) {
                            Log.v( TAG, "onPerformSync : adding Daily job, never run" );

                            jobs.add( job );
                        } else {
                            if( Days.daysBetween( lastRun, now ).getDays() >= 1 ) {
                                Log.v( TAG, "onPerformSync : adding Daily job" );

                                jobs.add( job );
                            }
                        }
                        break;
                    case WEEKLY:
                        if( status.equals( WorkItemConstants.Status.NEVER ) ) {
                            Log.v( TAG, "onPerformSync : adding Weekly job, never run" );

                            jobs.add( job );
                        } else {
                            if( Days.daysBetween( lastRun, now ).getDays() >= 7 ) {
                                Log.v( TAG, "onPerformSync : adding Weekly job" );
                                jobs.add( job );
                            }
                        }
                        break;
                }
            }
            cursor.close();
            Log.i( TAG, "onPerformSync : " + jobs.size() + " scheduled to run" );
            executeJobs( provider, jobs );
        } catch( RemoteException e ) {
            Log.e( TAG, "onPerformSync : error, RemoteException", e );

            result.hasHardError();
        } catch( IOException e ) {
            Log.e( TAG, "onPerformSync : error, IOException", e );

            result.hasHardError();
        } finally {
            Intent completeIntent = new Intent();
            completeIntent.setAction( COMPLETE_ACTION );
            mContext.sendBroadcast( completeIntent );
        }

        Log.i( TAG, "onPerformSync : exit" );
    }

    private void executeJobs( ContentProviderClient provider, List<Job> jobs ) throws RemoteException, IOException {
        Log.v( TAG, "executeJobs : enter" );

        if( !jobs.isEmpty() ) {
            for( Job job : jobs ) {
                switch( job.getType() ) {
                    case OVERVIEW:
                        Log.v( TAG, "executeJobs : refreshing shows" );

                        getShows( provider, job );
                        break;
                    case EVENTS:
                        Log.v( TAG, "executeJobs : refreshing events" );

                        getEvents( provider, job );
                        break;
                    case LIVE:
                        Log.v( TAG, "executeJobs : refreshing live status" );

                        getLives( provider, job );
                        break;
                    case LIST:
                        Log.v( TAG, "executeJobs : refreshing episode list" );

                        getEpisodes( provider, job );
                        break;

                    case RECENT:
                        Log.v( TAG, "executeJobs : refreshing recent episodes" );

                        getRecentEpisodes( provider, job );

                        break;

                    case IMAGE:
                        Log.v( TAG, "executeJobs : refreshing images" );

//                        saveImage( provider, job );

                        break;

                    case DETAILS:
                        Log.v( TAG, "executeJobs : refreshing episode details" );

                        getEpisodeDetails( provider, job );

                        break;

                    case YOUTUBE:
                        Log.v( TAG, "executeJobs : refreshing youtube episodes" );

                        getYoutubeEpisodes(provider, job);

                        break;

                    default:
                        Log.w( TAG, "executeJobs : Scheduled '" + job.getType().name() + "' not supported" );

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
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {

            if( wifiConnected || mobileConnected ) {
                Log.v( TAG, "getEpisodes : network is available" );

                JSONArray jsonArray = loadJsonArrayFromNetwork( job );
                Log.i( TAG, "getEpisodes : jsonArray=" + jsonArray.toString() );
                processEpisodes(jsonArray, provider, job.getType());
            }

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e(TAG, "getEpisodes : error", e);

            update.put(WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name());
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "getEpisodes : exit" );
    }

    private void getEpisodeDetails( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        Log.v( TAG, "getEpisodeDetails : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

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

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e(TAG, "getEpisodeDetails : error", e);

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "getEpisodeDetails : exit" );
    }

    private void getRecentEpisodes( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        Log.v( TAG, "getRecentEpisodes : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {

            if( wifiConnected || mobileConnected ) {
                Log.v(TAG, "getRecentEpisodes : network is available");

                JSONArray jsonArray = loadJsonArrayFromNetwork(job);
                Log.i( TAG, "getRecentEpisodes : jsonArray=" + jsonArray.toString() );
                processEpisodes(jsonArray, provider, job.getType());
            }

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e(TAG, "getRecentEpisodes : error", e);

            update.put(WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name());
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "getRecentEpisodes : exit" );
    }

    private void getShowDetails( ContentProviderClient provider, int showId ) throws RemoteException, IOException {
        Log.v( TAG, "getShowDetails : enter" );

        String address = "";
        Cursor cursor = provider.query( EndpointConstants.CONTENT_URI, null, EndpointConstants.FIELD_TYPE + "=?", new String[] { EndpointConstants.Type.DETAILS.name() }, null );

        while( cursor.moveToNext() ) {
            address = cursor.getString( cursor.getColumnIndex( EndpointConstants.FIELD_URL ) );
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

    private void getYoutubeEpisodes( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        Log.v( TAG, "getYoutubeEpisodes : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {

            if( wifiConnected || mobileConnected ) {
                Log.v( TAG, "getYoutubeEpisodes : network is available" );

                JSONObject json = loadJsonFromNetwork( job );
                if( null != json ) {
                    Log.i( TAG, "getYoutubeEpisodes : json=" + json.toString() );
                    processYoutubeEpisodes(json, provider, job);
                }
            }

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e( TAG, "getYoutubeEpisodes : error", e );

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "getYoutubeEpisodes : exit" );
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

            String line;
            while( ( line = reader.readLine() ) != null ) {
                sb.append(line).append("\n");
            }
            json = new JSONObject( sb.toString() );

            job.setStatus( WorkItemConstants.Status.OK );

        } catch( NullPointerException e ) {
            Log.e( TAG, "loadJsonFromNetwork : error", e );

            job.setStatus( WorkItemConstants.Status.FAILED );
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

            String line;
            while( ( line = reader.readLine() ) != null ) {
                sb.append(line).append("\n");
            }
            jsonArray = new JSONArray( sb.toString() );

            job.setStatus( WorkItemConstants.Status.OK );

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

    private Bitmap loadBitmapFromNetwork( Job job ) throws IOException {
        Log.v( TAG, "loadBitmapFromNetwork : enter" );

        Bitmap bitmap = null;
        InputStream stream = null;

        try {

            stream = downloadUrl( job );

            bitmap = BitmapFactory.decodeStream( stream );

            job.setStatus( WorkItemConstants.Status.OK );

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

//        long currentTime = System.currentTimeMillis();
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

                job.setStatus( WorkItemConstants.Status.NOT_MODIFIED );
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
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {
            int count = 0, loaded = 0;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { ShowConstants._ID };

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
                values.put( ShowConstants._ID, showNameId );
                values.put( ShowConstants.FIELD_NAME, name );
                values.put( ShowConstants.FIELD_PREFIX, prefix );
                values.put( ShowConstants.FIELD_VIP, vip );
                values.put( ShowConstants.FIELD_SORTORDER, sortOrder );
                values.put( ShowConstants.FIELD_DESCRIPTION, json.getString( "Description" ) );
                values.put( ShowConstants.FIELD_COVERIMAGEURL, coverImageUrl );
                values.put( ShowConstants.FIELD_COVERIMAGEURL_SQUARED, coverImageUrl );
                values.put( ShowConstants.FIELD_COVERIMAGEURL_100, coverImageUrl );
                values.put( ShowConstants.FIELD_COVERIMAGEURL_200, coverImageUrl );
                values.put( ShowConstants.FIELD_FORUMURL, json.getString( "ForumUrl" ) );
                values.put( ShowConstants.FIELD_PREVIEWURL, json.getString( "PreviewUrl" ) );
                values.put( ShowConstants.FIELD_EPISODE_COUNT, json.getInt( "EpisodeCount" ) );
                values.put( ShowConstants.FIELD_EPISODE_COUNT_MAX, json.getInt( "EpisodeNumberMax" ) );
                values.put( ShowConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                Cursor cursor = provider.query( ContentUris.withAppendedId( ShowConstants.CONTENT_URI, showNameId ), projection, null, null, null );
                if( cursor.moveToFirst() ) {
                    Log.v( TAG, "processShows : show iteration, updating existing entry" );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( ShowConstants._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( ShowConstants.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                } else {
                    Log.v( TAG, "processShows : show iteration, adding new entry" );

                    ops.add(
                            ContentProviderOperation.newInsert( ShowConstants.CONTENT_URI )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                }
                cursor.close();
                count++;

                if( showNameId ==  1 ) {
                    Log.v( TAG, "processShows : adding one time update for katg main show" );

                    values = new ContentValues();
                    values.put( WorkItemConstants.FIELD_NAME, "Refresh " + json.getString( "Name" ) );
                    values.put( WorkItemConstants.FIELD_FREQUENCY, WorkItemConstants.Frequency.ONCE.name() );
                    values.put( WorkItemConstants.FIELD_DOWNLOAD, WorkItemConstants.Download.JSONARRAY.name() );
                    values.put( WorkItemConstants.FIELD_ENDPOINT, EndpointConstants.Type.LIST.name() );
                    values.put( WorkItemConstants.FIELD_ADDRESS, EndpointConstants.LIST );
                    values.put( WorkItemConstants.FIELD_PARAMETERS, "?shownameid=" + showNameId );
                    values.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                    cursor = provider.query( WorkItemConstants.CONTENT_URI, null, WorkItemConstants.FIELD_ENDPOINT + " = ? and " + WorkItemConstants.FIELD_PARAMETERS + " = ?", new String[] { EndpointConstants.LIST, "?shownameid=" + showNameId }, null );
                    if( cursor.moveToNext() ) {
                        Log.v( TAG, "processShows : updating daily show" );

                        values.put( WorkItemConstants.FIELD_LAST_RUN, new DateTime( DateTimeZone.UTC ).getMillis() );

                        Long id = cursor.getLong( cursor.getColumnIndexOrThrow( WorkItemConstants._ID ) );
                        ops.add(
                                ContentProviderOperation.newUpdate( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, id ) )
                                        .withValues( values )
                                        .withYieldAllowed( true )
                                        .build()
                        );
                    } else {
                        Log.v( TAG, "processShows : adding daily show" );

                        values.put( WorkItemConstants.FIELD_LAST_RUN, -1 );
                        values.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.NEVER.name() );

                        ops.add(
                                ContentProviderOperation.newInsert( WorkItemConstants.CONTENT_URI )
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
                    values.put( WorkItemConstants.FIELD_NAME, "Refresh " + json.getString( "Name" ) );
                    values.put( WorkItemConstants.FIELD_FREQUENCY, WorkItemConstants.Frequency.DAILY.name() );
                    values.put( WorkItemConstants.FIELD_DOWNLOAD, WorkItemConstants.Download.JSONARRAY.name() );
                    values.put( WorkItemConstants.FIELD_ENDPOINT, EndpointConstants.Type.LIST.name() );
                    values.put( WorkItemConstants.FIELD_ADDRESS, EndpointConstants.LIST );
                    values.put( WorkItemConstants.FIELD_PARAMETERS, "?shownameid=" + showNameId );
                    values.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.NEVER.name() );
                    values.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                    cursor = provider.query( WorkItemConstants.CONTENT_URI, null, WorkItemConstants.FIELD_ADDRESS + " = ? and " + WorkItemConstants.FIELD_PARAMETERS + " = ?", new String[] { EndpointConstants.LIST, "?shownameid=" + showNameId }, null );
                    if( cursor.moveToNext() ) {
                        Log.v( TAG, "processShows : updating daily spinoff show" );

                        values.put( WorkItemConstants.FIELD_LAST_RUN, new DateTime( DateTimeZone.UTC ).getMillis() );

                        Long id = cursor.getLong( cursor.getColumnIndexOrThrow( WorkItemConstants._ID ) );
                        ops.add(
                                ContentProviderOperation.newUpdate( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, id ) )
                                        .withValues( values )
                                        .withYieldAllowed( true )
                                        .build()
                        );
                    } else {
                        Log.v( TAG, "processShows : adding daily spinoff show" );

                        values.put( WorkItemConstants.FIELD_LAST_RUN, -1 );
                        values.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.NEVER.name() );

                        ops.add(
                                ContentProviderOperation.newInsert( WorkItemConstants.CONTENT_URI )
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

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, job.getStatus().name() );
        } catch( Exception e ) {
            Log.e( TAG, "processShows : error", e );

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "processShows : exit" );
    }

    private void processEvents( JSONObject jsonObject, ContentProviderClient provider, Job job ) throws RemoteException {
        Log.v( TAG, "processEvents : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {
            int count = 0, loaded = 0;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { EventConstants._ID };

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
                values.put( EventConstants.FIELD_EVENTID, eventId );
                values.put( EventConstants.FIELD_TITLE, json.getString( "title" ) );
                values.put( EventConstants.FIELD_LOCATION, json.getString( "location" ) );
                values.put( EventConstants.FIELD_STARTDATE, startDate.getMillis() );
                values.put( EventConstants.FIELD_ENDDATE, endDate.getMillis() );
                values.put( EventConstants.FIELD_DETAILS, json.getString( "details" ) );
                values.put( EventConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                Cursor cursor = provider.query( EventConstants.CONTENT_URI, projection, EventConstants.FIELD_EVENTID + "=?", new String[] { eventId }, null );
                if( cursor.moveToFirst() ) {
                    Log.v( TAG, "processEvents : show iteration, updating existing entry" );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( EventConstants._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( EventConstants.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );
                } else {
                    Log.v( TAG, "processEvents : show iteration, adding new entry" );

                    ops.add(
                            ContentProviderOperation.newInsert( EventConstants.CONTENT_URI )
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

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e( TAG, "processEvents : error", e );

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v(TAG, "processEvents : exit");
    }

    private void processLives( JSONObject jsonObject, ContentProviderClient provider, Job job ) throws RemoteException {
        Log.v( TAG, "processLives : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put(WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis());

        int broadcasting;
        try {
            broadcasting = jsonObject.getBoolean( "broadcasting" ) ? 1 : 0;

            ContentValues values = new ContentValues();
            values.put( LiveConstants.FIELD_BROADCASTING, broadcasting );
            values.put( LiveConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

            provider.update( ContentUris.withAppendedId( LiveConstants.CONTENT_URI, 1 ), values, null, null );

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.v( TAG, "processLives : broadcasting format is not valid" );

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v( TAG, "processLives : exit" );
    }

    private void processEpisodes( JSONArray jsonArray, ContentProviderClient provider, EndpointConstants.Type type ) {
        Log.v( TAG, "processEpisodes : enter" );

        try {
            int loaded = 0;
            List<Integer> detailsQueue = new ArrayList<Integer>();

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { EpisodeConstants._ID, EpisodeConstants.FIELD_DOWNLOADED, EpisodeConstants.FIELD_PLAYED, EpisodeConstants.FIELD_LASTPLAYED };

            ContentValues values;

            for( int i = 0; i < jsonArray.length(); i++ ) {
                JSONObject json = jsonArray.getJSONObject( i );
                Log.v( TAG, "processEpisodes : json=" + json.toString() );

                int showId = json.getInt( "ShowId" );
                detailsQueue.add( showId );

                String videoFileUrl = "";
                try {
                    videoFileUrl = json.getString( "VideoFileUrl" );
                } catch( JSONException e ) {
                    Log.v( TAG, "processEpisodes : VideoFileUrl format is not valid or does not exist" );
                }

                String videoThumbnailUrl = "";
                try {
                    videoThumbnailUrl = json.getString( "VideoThumbnailUrl" );
                } catch( JSONException e ) {
                    Log.v( TAG, "processEpisodes : VideoThumbnailUrl format is not valid or does not exist" );
                }

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
                    length = json.getInt( "Length" );
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

                int episodeType = -1;
                try {
                    episodeType = json.getInt( "Type" );
                } catch( Exception e ) {
                    Log.v( TAG, "processEpisodes : Type format is not valid or not present" );
                }

                values = new ContentValues();
                values.put( EpisodeConstants._ID, showId );
                values.put( EpisodeConstants.FIELD_NUMBER, json.getInt( "Number" ) );
                values.put( EpisodeConstants.FIELD_TITLE, json.getString( "Title" ) );
                values.put( EpisodeConstants.FIELD_VIDEOFILEURL, videoFileUrl );
                values.put( EpisodeConstants.FIELD_VIDEOTHUMBNAILURL, videoThumbnailUrl );
                values.put( EpisodeConstants.FIELD_PREVIEWURL, previewUrl );
                values.put( EpisodeConstants.FIELD_FILEURL, fileUrl );
                values.put( EpisodeConstants.FIELD_FILENAME, fileName );
                values.put( EpisodeConstants.FIELD_LENGTH, length );
                values.put( EpisodeConstants.FIELD_FILESIZE, fileSize );
                values.put( EpisodeConstants.FIELD_TYPE, episodeType );
                values.put( EpisodeConstants.FIELD_PUBLIC, vip );
                values.put( EpisodeConstants.FIELD_POSTED, json.getString( "PostedDate" ) );
                values.put( EpisodeConstants.FIELD_TIMESTAMP, json.getLong( "Timestamp") );
                values.put( EpisodeConstants.FIELD_SHOWNAMEID, showNameId );
                values.put( EpisodeConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                Cursor cursor = provider.query( ContentUris.withAppendedId( EpisodeConstants.CONTENT_URI, showId ), projection, null, null, null );
                if( cursor.moveToFirst() ) {
                    Log.v( TAG, "processEpisodes : episode iteration, updating existing entry" );

                    long downloaded = cursor.getLong( cursor.getColumnIndex( EpisodeConstants.FIELD_DOWNLOADED ) );
                    long played = cursor.getLong( cursor.getColumnIndex( EpisodeConstants.FIELD_PLAYED ) );
                    long lastplayed = cursor.getLong( cursor.getColumnIndex( EpisodeConstants.FIELD_LASTPLAYED ) );

                    values.put( EpisodeConstants.FIELD_DOWNLOADED, downloaded );
                    values.put( EpisodeConstants.FIELD_PLAYED, played );
                    values.put( EpisodeConstants.FIELD_LASTPLAYED, lastplayed );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( EpisodeConstants._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( EpisodeConstants.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                } else {
                    Log.v( TAG, "processEpisodes : episode iteration, adding new entry" );

                    values.put( EpisodeConstants.FIELD_DOWNLOADED, -1 );
                    values.put( EpisodeConstants.FIELD_PLAYED, -1 );
                    values.put( EpisodeConstants.FIELD_LASTPLAYED, -1 );

                    ops.add(
                            ContentProviderOperation.newInsert( EpisodeConstants.CONTENT_URI )
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
                        values.put( GuestConstants._ID, showGuestId );
                        values.put( GuestConstants.FIELD_REALNAME, name );
                        values.put( GuestConstants.FIELD_DESCRIPTION, guest.getString( "Description" ) );
                        values.put( GuestConstants.FIELD_PICTUREFILENAME, guest.getString( "PictureFilename" ) );
                        values.put( GuestConstants.FIELD_URL1, guest.getString( "Url1" ) );
                        values.put( GuestConstants.FIELD_URL2, guest.getString( "Url2" ) );
                        values.put( GuestConstants.FIELD_PICTUREURL, pictureUrl );
                        values.put( GuestConstants.FIELD_PICTUREURLLARGE, pictureUrlLarge );
                        values.put( GuestConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                        cursor = provider.query( ContentUris.withAppendedId( GuestConstants.CONTENT_URI, showGuestId ), null, null, null, null );
                        if( cursor.moveToFirst() ) {
                            Log.v( TAG, "processEpisodes : guest iteration, updating existing entry" );

                            Long id = cursor.getLong(cursor.getColumnIndexOrThrow( GuestConstants._ID ) );
                            ops.add(
                                    ContentProviderOperation.newUpdate( ContentUris.withAppendedId( GuestConstants.CONTENT_URI, id ) )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );

                        } else {
                            Log.v( TAG, "processEpisodes : guest iteration, adding new entry" );

                            ops.add(
                                    ContentProviderOperation.newInsert( GuestConstants.CONTENT_URI )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );
                        }
                        cursor.close();

                        values = new ContentValues();
                        values.put( EpisodeGuestConstants.FIELD_SHOWID, showId );
                        values.put( EpisodeGuestConstants.FIELD_SHOWGUESTID, showGuestId );
                        values.put( GuestConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                        cursor = provider.query( EpisodeGuestConstants.CONTENT_URI, null, EpisodeGuestConstants.FIELD_SHOWID + "=? and " + EpisodeGuestConstants.FIELD_SHOWGUESTID + "=?", new String[] { String.valueOf( showId ), String.valueOf( showGuestId ) }, null );
                        if( cursor.moveToFirst() ) {
                            Log.v( TAG, "processEpisodes : episodeGuest iteration, updating existing entry" );

                            Long id = cursor.getLong( cursor.getColumnIndexOrThrow( EpisodeGuestConstants._ID ) );
                            ops.add(
                                    ContentProviderOperation.newUpdate( ContentUris.withAppendedId( EpisodeGuestConstants.CONTENT_URI, id ) )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );

                        } else {
                            Log.v( TAG, "processEpisodes : episodeGuest iteration, adding new entry" );

                            ops.add(
                                    ContentProviderOperation.newInsert( EpisodeGuestConstants.CONTENT_URI )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );
                        }
                        cursor.close();
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

            if( EndpointConstants.Type.RECENT.equals( type ) ) {
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
            String[] projection = new String[] { DetailConstants._ID };

            ContentValues values = new ContentValues();
            values.put( DetailConstants.FIELD_NOTES, jsonObject.getString( "notes" ) );
            values.put( DetailConstants.FIELD_FORUMURL, jsonObject.getString( "forum_url" ) );
            values.put( DetailConstants.FIELD_SHOWID, showId );
            values.put( DetailConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

            Cursor cursor = provider.query( DetailConstants.CONTENT_URI, projection, DetailConstants.FIELD_SHOWID + "=?", new String[] { String.valueOf( showId ) }, null );
            if( cursor.moveToFirst() ) {
                Log.v( TAG, "processEpisodeDetails : detail iteration, updating existing entry" );

                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( DetailConstants._ID ) );
                ops.add(
                        ContentProviderOperation.newUpdate( ContentUris.withAppendedId( DetailConstants.CONTENT_URI, id ) )
                                .withValues( values )
                                .withYieldAllowed( true )
                                .build()
                );
            } else {
                Log.v( TAG, "processEpisodeDetails : detail iteration, adding new entry" );

                ops.add(
                        ContentProviderOperation.newInsert( DetailConstants.CONTENT_URI )
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

                boolean explicit = false;
                try {
                    explicit = json.getBoolean( "explicit" );
                } catch( Exception e ) {
                    Log.v( TAG, "processEpisodes : Public format is not valid or not present" );
                }

                values = new ContentValues();
                values.put( ImageConstants._ID, pictureid );
                values.put( ImageConstants.FIELD_TITLE, json.getString( "title" ) );
                values.put( ImageConstants.FIELD_DESCRIPTION, json.getString( "description" ) );
                values.put( ImageConstants.FIELD_EXPLICIT, explicit ? 1 : 0 );
                values.put( ImageConstants.FIELD_DISPLAY_ORDER, json.getInt( "displayorder" ) );
                values.put( ImageConstants.FIELD_MEDIAURL, json.getString( "media_url" ) );
                values.put( ImageConstants.FIELD_SHOWID, showId );
                values.put( ImageConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                cursor = provider.query( ContentUris.withAppendedId( ImageConstants.CONTENT_URI, pictureid ), null, null, null, null );
                if( cursor.moveToFirst() ) {
                    Log.v( TAG, "processEpisodeDetails : image iteration, updating existing entry" );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( ImageConstants._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( ImageConstants.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );
                } else {
                    Log.v( TAG, "processEpisodeDetails : image iteration, adding new entry" );

                    ops.add(
                            ContentProviderOperation.newInsert( ImageConstants.CONTENT_URI )
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

    private void processYoutubeEpisodes( JSONObject jsonObject, ContentProviderClient provider, Job job ) throws RemoteException, OperationApplicationException, JSONException {
        Log.v( TAG, "processYoutubeEpisodes : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        DateTime now = new DateTime( DateTimeZone.UTC );

        try {
            int count = 0, loaded = 0;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { YoutubeConstants._ID };

            ContentValues values;

            JSONObject feed = jsonObject.getJSONObject( "feed" );
            //Log.v( TAG, "processYoutubeEpisodes : feed=" + feed.toString() );
            if( null != feed ) {

                if( null != feed.getJSONArray( "entry" ) && feed.getJSONArray( "entry" ).length() > 0 ) {

                    JSONObject json;

                    for( int i = 0; i < feed.getJSONArray( "entry" ).length(); i++ ) {

                        json = feed.getJSONArray( "entry" ).getJSONObject( i );
                        if( null != json ) {
                            //Log.v( TAG, "processYoutubeEpisodes : json=" + json.toString() );

                            String content = "", thumbnail = "";
                            try {
                                content = json.getJSONObject( "content" ).getString( "$t" );
                            } catch( JSONException e ) {
                                Log.v( TAG, "processYoutubeEpisodes : content is not valid" );
                            }

                            if( !"".equals( content ) ) {
                                String img = content.substring( content.indexOf( "<img" ) );
                                img = img.substring( 0, img.indexOf( "</a>" ) - 2 );

                                thumbnail = img.substring( img.indexOf( "src=" ) + 5 );
                            }

                            String etag = "";
                            try {
                                etag = json.getString( "gd$etag" );
                            } catch( JSONException e ) {
                                Log.v( TAG, "processYoutubeEpisodes : etag is not valid" );
                            }

                            String youtubeId = "";
                            try {
                                youtubeId = json.getJSONObject( "id" ).getString( "$t" );
                            } catch( JSONException e ) {
                                Log.v( TAG, "processYoutubeEpisodes : id is not valid" );
                            } finally {
                                if( !"".equals( youtubeId ) ) {
                                    youtubeId = youtubeId.substring( youtubeId.lastIndexOf( ':' ) + 1 );
                                }
                            }

                            DateTime published = new DateTime();
                            try {
                                published = formaty.parseDateTime(json.getJSONObject( "published" ).getString( "$t" ) );
                            } catch( JSONException e ) {
                                Log.v( TAG, "processYoutubeEpisodes : published is not valid" );
                            } finally {
                                published = published.withZone( DateTimeZone.UTC );
                            }

                            DateTime updated = new DateTime();
                            try {
                                updated = formaty.parseDateTime( json.getJSONObject( "updated" ).getString( "$t" ) );
                            } catch( JSONException e ) {
                                Log.v( TAG, "processYoutubeEpisodes : updated is not valid" );
                            } finally {
                                updated = updated.withZone( DateTimeZone.UTC );
                            }

                            String title = "";
                            try {
                                title = json.getJSONObject( "title" ).getString( "$t");
                            } catch( JSONException e ) {
                                Log.v( TAG, "processYoutubeEpisodes : title is not valid" );
                            }

                            String link = "";
                            if( null != json.getJSONArray( "link" ) && json.getJSONArray( "link" ).length() > 0 ) {

                                for( int linkIdx = 0; linkIdx < json.getJSONArray( "link" ).length() - 1; linkIdx++ ) {

                                    JSONObject aLink = json.getJSONArray( "link" ).getJSONObject( linkIdx );
                                    //Log.v( TAG, "processYoutubeEpisodes : link=" + aLink.toString() );

                                    if( "alternate".equals(aLink.getString("rel")) ) {
                                        link = aLink.getString( "href" );
                                    }
                                }
                            }

                            values = new ContentValues();
                            values.put( YoutubeConstants.FIELD_YOUTUBE_ID, youtubeId );
                            values.put( YoutubeConstants.FIELD_YOUTUBE_ETAG, etag );
                            values.put( YoutubeConstants.FIELD_YOUTUBE_TITLE, title );
                            values.put( YoutubeConstants.FIELD_YOUTUBE_LINK, link );
                            values.put( YoutubeConstants.FIELD_YOUTUBE_THUMBNAIL, thumbnail );
                            values.put( YoutubeConstants.FIELD_YOUTUBE_PUBLISHED, published.getMillis() );
                            values.put( YoutubeConstants.FIELD_YOUTUBE_UPDATED, updated.getMillis() );
                            values.put( YoutubeConstants.FIELD_LAST_MODIFIED_DATE, now.getMillis() );

                            Cursor cursor = provider.query( YoutubeConstants.CONTENT_URI, projection, YoutubeConstants.FIELD_YOUTUBE_ID + " = ?", new String[] { youtubeId }, null );
                            if( cursor.moveToFirst() ) {
                                Log.v( TAG, "processYoutubeEpisodes : updating existing entry" );

                                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( YoutubeConstants._ID ) );
                                ops.add(
                                        ContentProviderOperation.newUpdate( ContentUris.withAppendedId( YoutubeConstants.CONTENT_URI, id ) )
                                                .withValues( values )
                                                .withYieldAllowed( true )
                                                .build()
                                );
                            } else {
                                Log.v( TAG, "processYoutubeEpisodes : adding new entry" );

                                ops.add(
                                        ContentProviderOperation.newInsert( YoutubeConstants.CONTENT_URI )
                                                .withValues( values )
                                                .withYieldAllowed( true )
                                                .build()
                                );
                            }
                            cursor.close();
                            count++;

                            if( count > 100 ) {
                                Log.v( TAG, "processYoutubeEpisodes : applying batch for '" + count + "' transactions" );

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
                    }
                }
            }

            if( !ops.isEmpty() ) {
                Log.v( TAG, "processYoutubeEpisodes : applying final batch for '" + count + "' transactions" );

                ContentProviderResult[] results = provider.applyBatch( ops );
                loaded += results.length;

                if( results.length > 0 ) {
                    ops.clear();
                }
            }

            mContext.getContentResolver().delete( YoutubeConstants.CONTENT_URI, YoutubeConstants.FIELD_LAST_MODIFIED_DATE + " != ?", new String[] { String.valueOf( now.getMillis() ) } );

            Log.i( TAG, "processYoutubeEpisodes : events loaded '" + loaded + "'" );

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, job.getStatus().name() );

        } catch( Exception e ) {
            Log.e( TAG, "processYoutubeEpisodes : error", e );

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        Log.v(TAG, "processYoutubeEpisodes : exit");
    }

    private class Job {

        private Long id;
        private EndpointConstants.Type type;
        private WorkItemConstants.Download download;
        private String url;
        private String filename;
        private String etag;
        private WorkItemConstants.Status status;

        public Long getId() { return id; }

        public void setId( Long id ) { this.id = id; }

        public WorkItemConstants.Download getDownload() { return download; }

        public void setDownload( WorkItemConstants.Download download ) { this.download = download; }

        public EndpointConstants.Type getType() {
            return type;
        }

        public void setType( EndpointConstants.Type type ) {
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

        public WorkItemConstants.Status getStatus() {
            return status;
        }

        public void setStatus( WorkItemConstants.Status status ) {
            this.status = status;
        }

        public String getEtag() {
            return etag;
        }

        public void setEtag( String etag ) {
            this.etag = etag;
        }

        @Override
        public String toString() {
            return "Job{" +
                    "id=" + id +
                    ", type=" + type +
                    ", download=" + download +
                    ", url='" + url + '\'' +
                    ", filename='" + filename + '\'' +
                    ", etag='" + etag + '\'' +
                    ", status=" + status +
                    '}';
        }

    }

}
