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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
//import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.keithandthegirl.app.db.model.Detail;
import com.keithandthegirl.app.db.model.DetailConstants;
import com.keithandthegirl.app.db.model.EndpointConstants;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.EpisodeGuestConstants;
import com.keithandthegirl.app.db.model.Event;
import com.keithandthegirl.app.db.model.EventConstants;
import com.keithandthegirl.app.db.model.Events;
import com.keithandthegirl.app.db.model.Guest;
import com.keithandthegirl.app.db.model.GuestConstants;
import com.keithandthegirl.app.db.model.Image;
import com.keithandthegirl.app.db.model.ImageConstants;
import com.keithandthegirl.app.db.model.Live;
import com.keithandthegirl.app.db.model.LiveConstants;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.db.model.WorkItemConstants;
import com.keithandthegirl.app.db.model.Youtube;
import com.keithandthegirl.app.db.model.YoutubeConstants;
import com.keithandthegirl.app.db.model.YoutubeEntry;
import com.keithandthegirl.app.db.model.YoutubeLink;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 *
 * Created by dmfrey on 3/10/14.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

//    private static final String TAG = SyncAdapter.class.getSimpleName();

    public static final String START_ACTION = "com.keithandthegirl.app.sync.START_ACTION";
    public static final String COMPLETE_ACTION = "com.keithandthegirl.app.sync.COMPLETE_ACTION";

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;

    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    SharedPreferences mSharedPreferences;
    Context mContext;

    KatgService katgService;
    YoutubeService youtubeService;

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

        initializeService();
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

        initializeService();
    }

    private void initializeService() {

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
//            .setLogLevel( RestAdapter.//LogLevel.FULL )
            .build();

        katgService = katgRestAdapter.create( KatgService.class );

        Gson youtubeGson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        RestAdapter youtubeRestAdapter = new RestAdapter.Builder()
                .setEndpoint( YoutubeService.YOUTUBE_KATG_URL )
                .setClient( new OkClient( client ) )
                .setConverter( new GsonConverter( youtubeGson ) )
//                .setLogLevel( RestAdapter.//LogLevel.FULL )
                .build();

        youtubeService = youtubeRestAdapter.create( YoutubeService.class );

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

    }

    /*
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync( Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult ) {
        //Log.i( TAG, "onPerformSync : enter" );

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

                //Log.i( TAG, "onPerformSync : job=" + job.toString() );

                switch( wtype ) {
                    case ON_DEMAND:
                        if( !status.equals( WorkItemConstants.Status.OK ) ) {
                            //Log.i( TAG, "onPerformSync : adding On Demand job" );

                            jobs.add( job );
                        }
                        break;
                    case ONCE:
                        if( !status.equals( WorkItemConstants.Status.OK ) ) {
                            //Log.i( TAG, "onPerformSync : adding One Time job" );

                            jobs.add( job );
                        }
                        break;
                    case HOURLY:
                        if( status.equals( WorkItemConstants.Status.NEVER ) ) {
                            //Log.i( TAG, "onPerformSync : adding Hourly job, never run" );

                            jobs.add( job );
                        } else {
                            if( Minutes.minutesBetween( lastRun, now ).getMinutes() >= 60 ) {
                                //Log.i( TAG, "onPerformSync : adding Hourly job" );

                                jobs.add( job );
                            }
                        }
                        break;
                    case DAILY:
                        if( status.equals( WorkItemConstants.Status.NEVER ) ) {
                            //Log.i( TAG, "onPerformSync : adding Daily job, never run" );

                            jobs.add( job );
                        } else {
                            if( Days.daysBetween( lastRun, now ).getDays() >= 1 ) {
                                //Log.i( TAG, "onPerformSync : adding Daily job" );

                                jobs.add( job );
                            }
                        }
                        break;
                    case WEEKLY:
                        if( status.equals( WorkItemConstants.Status.NEVER ) ) {
                            //Log.i( TAG, "onPerformSync : adding Weekly job, never run" );

                            jobs.add( job );
                        } else {
                            if( Days.daysBetween( lastRun, now ).getDays() >= 7 ) {
                                //Log.i( TAG, "onPerformSync : adding Weekly job" );
                                jobs.add( job );
                            }
                        }
                        break;
                }
            }
            cursor.close();
            //Log.i( TAG, "onPerformSync : " + jobs.size() + " scheduled to run" );
            executeJobs( provider, jobs );
        } catch( RemoteException e ) {
            //Log.e( TAG, "onPerformSync : error, RemoteException", e );

            result.hasHardError();
        } catch( IOException e ) {
            //Log.e( TAG, "onPerformSync : error, IOException", e );

            result.hasHardError();
        } finally {
            Intent completeIntent = new Intent();
            completeIntent.setAction( COMPLETE_ACTION );
            mContext.sendBroadcast( completeIntent );
        }

        //Log.i( TAG, "onPerformSync : exit" );
    }

    private void executeJobs( ContentProviderClient provider, List<Job> jobs ) throws RemoteException, IOException {
        //Log.v( TAG, "executeJobs : enter" );

        if( !jobs.isEmpty() ) {
            for( Job job : jobs ) {
                switch( job.getType() ) {
                    case OVERVIEW:
                        //Log.i( TAG, "executeJobs : refreshing shows" );

                        getShows( provider, job );
                        break;
                    case EVENTS:
                        //Log.i( TAG, "executeJobs : refreshing events" );

                        getEvents( provider, job );
                        break;
                    case LIVE:
                        //Log.i( TAG, "executeJobs : refreshing live status" );

                        getLives( provider, job );
                        break;
                    case LIST:
                        //Log.i( TAG, "executeJobs : refreshing episode list" );

                        getEpisodes( provider, job );
                        break;

                    case RECENT:
                        //Log.i( TAG, "executeJobs : refreshing recent episodes" );

                        getRecentEpisodes( provider, job );

                        break;

                    case IMAGE:
                        //Log.i( TAG, "executeJobs : refreshing images" );

//                        saveImage( provider, job );

                        break;

                    case DETAILS:
                        //Log.i( TAG, "executeJobs : refreshing episode details" );

                        getEpisodeDetails( provider, job );

                        break;

                    case YOUTUBE:
                        //Log.i( TAG, "executeJobs : refreshing youtube episodes" );

                        getYoutubeEpisodes( provider, job );

                        break;

                    default:
                        //Log.w( TAG, "executeJobs : Scheduled '" + job.getType().name() + "' not supported" );

                }
            }
        }

        //Log.v( TAG, "executeJobs : exit" );
    }

    private void getShows( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        //Log.v( TAG, "getShows : enter" );

        try {

            if( wifiConnected || mobileConnected ) {
                //Log.v( TAG, "getShows : network is available" );

                List<Show> shows = katgService.seriesOverview();
                if( null != shows && !shows.isEmpty() ) {

                    processShows( shows, provider, job );

                }

            }

        } catch( Exception e ) {
            //Log.e(TAG, "getShows : error", e);
        }

        //Log.v( TAG, "getShows : exit" );
    }

    private void getEvents( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        //Log.v( TAG, "getEvents : enter" );

        try {

            if( wifiConnected || mobileConnected ) {
                //Log.v( TAG, "getEvents : network is available" );

                Events events = katgService.events();
                if( null != events ) {

                    ////Log.v( TAG, "getEvents : event=" + events.toString() );
                    processEvents(events, provider, job);

                }

            }

        } catch( Exception e ) {
            //Log.e(TAG, "getEvents : error", e);
        }

        //Log.v( TAG, "getEvents : exit" );
    }

    private void getLives( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        //Log.v( TAG, "getLives : enter" );

        try {

            if( wifiConnected || mobileConnected ) {
                //Log.v( TAG, "getEvents : network is available" );

                Live live = katgService.broadcasting();

                //Log.i(TAG, "getLives : live=" + live.toString());
                processBroadcasting( live, provider, job );
            }

        } catch( Exception e ) {
            //Log.e(TAG, "getLives : error", e);
        }

        //Log.v( TAG, "getLives : exit" );
    }

    private void getEpisodes( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        //Log.v( TAG, "getEpisodes : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {

            if( wifiConnected || mobileConnected ) {
                //Log.v( TAG, "getEpisodes : network is available" );

                Uri uri = Uri.parse( job.getUrl() );
                int showNameId = Integer.parseInt( uri.getQueryParameter( "shownameid" ) );
                int showId = -1, number = -1;

                try {
                    showId = Integer.parseInt( uri.getQueryParameter( "showid" ) );
                } catch( NumberFormatException e ) { }

                try {
                    number = Integer.parseInt(uri.getQueryParameter( "number" ) );
                } catch( NumberFormatException e ) { }

                List<Episode> episodes = katgService.listEpisodes( showNameId, showId, number );

                processEpisodes( episodes, provider, job.getType() );
            }

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.OK.name() );

        } catch( Exception e ) {
            //Log.e(TAG, "getEpisodes : error", e);

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        //Log.v( TAG, "getEpisodes : exit" );
    }

    private void getEpisodeDetails( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        //Log.v( TAG, "getEpisodeDetails : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {

            if( wifiConnected || mobileConnected ) {
                //Log.v( TAG, "getEpisodeDetails : network is available" );

                Uri uri = Uri.parse( job.getUrl() );
                int showId = Integer.parseInt( uri.getQueryParameter( "showid" ) );

                Detail showDetails = katgService.showDetails(showId, 0);
                if( null != showDetails ) {
                    //Log.i(TAG, "getEpisodeDetails : showDetails=" + showDetails.toString());
                    processEpisodeDetails(showDetails, provider, showId);
                }
            }

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.OK.name() );

        } catch( Exception e ) {
            //Log.e(TAG, "getEpisodeDetails : error", e);

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        //Log.v( TAG, "getEpisodeDetails : exit" );
    }

    private void getRecentEpisodes( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        //Log.v( TAG, "getRecentEpisodes : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {

            if( wifiConnected || mobileConnected ) {
                //Log.v(TAG, "getRecentEpisodes : network is available");

                List<Episode> recentEpisodes = katgService.recentEpisodes();
                if( null != recentEpisodes && !recentEpisodes.isEmpty() ) {

                    processEpisodes( recentEpisodes, provider, job.getType() );

                }

            }

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.OK.name() );

        } catch( Exception e ) {
            //Log.e(TAG, "getRecentEpisodes : error", e);

            update.put(WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name());
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        //Log.v( TAG, "getRecentEpisodes : exit" );
    }

    private void getShowDetails( ContentProviderClient provider, int showId ) throws RemoteException, IOException {
        //Log.v( TAG, "getShowDetails : enter" );

        String address = "";
        Cursor cursor = provider.query( EndpointConstants.CONTENT_URI, null, EndpointConstants.FIELD_TYPE + "=?", new String[] { EndpointConstants.Type.DETAILS.name() }, null );

        while( cursor.moveToNext() ) {
            address = cursor.getString( cursor.getColumnIndex( EndpointConstants.FIELD_URL ) );
        }
        cursor.close();

        try {

            if( wifiConnected || mobileConnected ) {
                //Log.v( TAG, "getShowDetails : network is available" );

                Job job = new Job();
                job.setUrl( address + "?showid=" + showId );

                Detail showDetails = katgService.showDetails( showId, 1 );
                if( null != showDetails ) {

                    processEpisodeDetails( showDetails, provider, showId );

                }

            }

        } catch( Exception e ) {
            //Log.e(TAG, "getShowDetails : error", e);
        }

        //Log.v( TAG, "getShowDetails : exit" );
    }

    private void getYoutubeEpisodes( ContentProviderClient provider, Job job ) throws RemoteException, IOException {
        //Log.v( TAG, "getYoutubeEpisodes : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {

            if( wifiConnected || mobileConnected ) {
                //Log.v( TAG, "getYoutubeEpisodes : network is available" );

                Youtube youtube = youtubeService.listKatgYoutubeFeed();
                if( null != youtube ) {
                    //Log.i( TAG, "getYoutubeEpisodes : youtube=" + youtube.toString() );

                    processYoutubeEpisodes( youtube, provider, job );
                }
            }

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.OK.name() );

        } catch( Exception e ) {
            //Log.e( TAG, "getYoutubeEpisodes : error", e );

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        //Log.v( TAG, "getYoutubeEpisodes : exit" );
    }

    private void processShows( List<Show> shows, ContentProviderClient provider, Job job ) throws RemoteException {
        //Log.v( TAG, "processShows : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {
            int count = 0, loaded = 0;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { ShowConstants._ID };

            ContentValues values;

            for( Show show : shows ) {
                //Log.v( TAG, "processShows : show=" + show.toString() );

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

                Cursor cursor = provider.query( ContentUris.withAppendedId( ShowConstants.CONTENT_URI, show.getShowNameId() ), projection, null, null, null );
                if( cursor.moveToFirst() ) {
                    //Log.v( TAG, "processShows : show iteration, updating existing entry" );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( ShowConstants._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( ShowConstants.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                } else {
                    //Log.v( TAG, "processShows : show iteration, adding new entry" );

                    ops.add(
                            ContentProviderOperation.newInsert( ShowConstants.CONTENT_URI )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );

                }
                cursor.close();
                count++;

                if( show.getShowNameId() ==  1 ) {
                    //Log.v( TAG, "processShows : adding one time update for katg main show" );

                    values = new ContentValues();
                    values.put( WorkItemConstants.FIELD_NAME, "Refresh " + show.getName() );
                    values.put( WorkItemConstants.FIELD_FREQUENCY, WorkItemConstants.Frequency.ONCE.name() );
                    values.put( WorkItemConstants.FIELD_DOWNLOAD, WorkItemConstants.Download.JSONARRAY.name() );
                    values.put( WorkItemConstants.FIELD_ENDPOINT, EndpointConstants.Type.LIST.name() );
                    values.put( WorkItemConstants.FIELD_ADDRESS, EndpointConstants.LIST );
                    values.put( WorkItemConstants.FIELD_PARAMETERS, "?shownameid=" + show.getShowNameId() );
                    values.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                    cursor = provider.query( WorkItemConstants.CONTENT_URI, null, WorkItemConstants.FIELD_ENDPOINT + " = ? and " + WorkItemConstants.FIELD_PARAMETERS + " = ?", new String[] { EndpointConstants.LIST, "?shownameid=" + show.getShowNameId() }, null );
                    if( cursor.moveToNext() ) {
                        //Log.v( TAG, "processShows : updating daily show" );

                        values.put( WorkItemConstants.FIELD_LAST_RUN, new DateTime( DateTimeZone.UTC ).getMillis() );

                        Long id = cursor.getLong( cursor.getColumnIndexOrThrow( WorkItemConstants._ID ) );
                        ops.add(
                                ContentProviderOperation.newUpdate( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, id ) )
                                        .withValues( values )
                                        .withYieldAllowed( true )
                                        .build()
                        );
                    } else {
                        //Log.v( TAG, "processShows : adding daily show" );

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

                if( !"KATG".equals( show.getPrefix() ) ) {
                    //Log.v( TAG, "processShows : adding daily updates for spinoff shows" );

                    values = new ContentValues();
                    values.put( WorkItemConstants.FIELD_NAME, "Refresh " + show.getName() );
                    values.put( WorkItemConstants.FIELD_FREQUENCY, WorkItemConstants.Frequency.DAILY.name() );
                    values.put( WorkItemConstants.FIELD_DOWNLOAD, WorkItemConstants.Download.JSONARRAY.name() );
                    values.put( WorkItemConstants.FIELD_ENDPOINT, EndpointConstants.Type.LIST.name() );
                    values.put( WorkItemConstants.FIELD_ADDRESS, EndpointConstants.LIST );
                    values.put( WorkItemConstants.FIELD_PARAMETERS, "?shownameid=" + show.getShowNameId() );
                    values.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.NEVER.name() );
                    values.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                    cursor = provider.query( WorkItemConstants.CONTENT_URI, null, WorkItemConstants.FIELD_ADDRESS + " = ? and " + WorkItemConstants.FIELD_PARAMETERS + " = ?", new String[] { EndpointConstants.LIST, "?shownameid=" + show.getShowNameId() }, null );
                    if( cursor.moveToNext() ) {
                        //Log.v( TAG, "processShows : updating daily spinoff show" );

                        values.put( WorkItemConstants.FIELD_LAST_RUN, new DateTime( DateTimeZone.UTC ).getMillis() );

                        Long id = cursor.getLong( cursor.getColumnIndexOrThrow( WorkItemConstants._ID ) );
                        ops.add(
                                ContentProviderOperation.newUpdate( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, id ) )
                                        .withValues( values )
                                        .withYieldAllowed( true )
                                        .build()
                        );
                    } else {
                        //Log.v( TAG, "processShows : adding daily spinoff show" );

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
                    //Log.v( TAG, "processShows : applying batch for '" + count + "' transactions" );

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
                //Log.v( TAG, "processShows : applying final batch for '" + count + "' transactions" );

                ContentProviderResult[] results = provider.applyBatch( ops );
                loaded += results.length;

                if( results.length > 0 ) {
                    ops.clear();
                }
            }

            //Log.i( TAG, "processShows : shows loaded '" + loaded + "'" );

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.OK.name() );
        } catch( Exception e ) {
            //Log.e( TAG, "processShows : error", e );

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        //Log.v( TAG, "processShows : exit" );
    }

    private void processEvents( Events events, ContentProviderClient provider, Job job ) throws RemoteException {
        //Log.v( TAG, "processEvents : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {
            int count = 0, loaded = 0;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
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

                Cursor cursor = provider.query( EventConstants.CONTENT_URI, projection, EventConstants.FIELD_EVENTID + "=?", new String[] { event.getEventId() }, null );
                if( cursor.moveToFirst() ) {
                    //Log.v( TAG, "processEvents : event iteration, updating existing entry" );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( EventConstants._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( EventConstants.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );
                } else {
                    //Log.v( TAG, "processEvents : event iteration, adding new entry" );

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
                    //Log.v( TAG, "processEvents : applying batch for '" + count + "' transactions" );

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
                //Log.v( TAG, "processEvents : applying final batch for '" + count + "' transactions" );

                ContentProviderResult[] results = provider.applyBatch( ops );
                loaded += results.length;

                if( results.length > 0 ) {
                    ops.clear();
                }
            }

            //Log.i( TAG, "processEvents : events loaded '" + loaded + "'" );

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.OK.name() );

        } catch( Exception e ) {
            //Log.e( TAG, "processEvents : error", e );

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        //Log.v( TAG, "processEvents : exit" );
    }

    private void processBroadcasting( Live live, ContentProviderClient provider, Job job ) throws RemoteException {
        //Log.v( TAG, "processBroadcasting : enter" );

        DateTime lastRun = new DateTime( DateTimeZone.UTC );
        ContentValues update = new ContentValues();
        update.put( WorkItemConstants._ID, job.getId() );
        update.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, lastRun.getMillis() );

        try {

            ContentValues values = new ContentValues();
            values.put( LiveConstants.FIELD_BROADCASTING, live.isBroadcasting() ? 1 : 0 );
            values.put( LiveConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

            provider.update( ContentUris.withAppendedId( LiveConstants.CONTENT_URI, 1 ), values, null, null );

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.OK.name() );

        } catch( Exception e ) {
            //Log.v( TAG, "processBroadcasting : broadcasting format is not valid" );

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        //Log.v( TAG, "processBroadcasting : exit" );
    }

    private void processEpisodes( List<Episode> episodes, ContentProviderClient provider, EndpointConstants.Type type ) {
        //Log.v( TAG, "processEpisodes : enter" );

        try {
            int loaded = 0;
            List<Integer> detailsQueue = new ArrayList<Integer>();

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { EpisodeConstants._ID, EpisodeConstants.FIELD_DOWNLOADED, EpisodeConstants.FIELD_PLAYED, EpisodeConstants.FIELD_LASTPLAYED };

            ContentValues values;

            for( Episode episode : episodes ) {
                //Log.v( TAG, "processEpisodes : episode=" + episode.toString() );

                detailsQueue.add( episode.getShowId() );

                String fileName = "";
                try {

                    Uri fileUrl = Uri.parse( episode.getFileUrl() );
                    if( null != fileUrl.getLastPathSegment() ) {
                        fileName = fileUrl.getLastPathSegment();
                        //Log.v( TAG, "processEpisodes : fileName=" + fileName );
                    }

                } catch( NullPointerException e ) { }

                values = new ContentValues();
                values.put( EpisodeConstants._ID, episode.getShowId() );
                values.put( EpisodeConstants.FIELD_NUMBER, episode.getNumber() );
                values.put( EpisodeConstants.FIELD_TITLE, episode.getTitle() );
                values.put( EpisodeConstants.FIELD_VIDEOFILEURL, episode.getVideoFileUrl() );
                values.put( EpisodeConstants.FIELD_VIDEOTHUMBNAILURL, episode.getVideoThumbnailUrl() );
                values.put( EpisodeConstants.FIELD_PREVIEWURL, episode.getPreviewUrl() );
                values.put( EpisodeConstants.FIELD_FILEURL, episode.getFileUrl() );
                values.put( EpisodeConstants.FIELD_FILENAME, fileName );
                values.put( EpisodeConstants.FIELD_LENGTH, episode.getLength() );
                values.put( EpisodeConstants.FIELD_FILESIZE, episode.getFileSize() );
                values.put( EpisodeConstants.FIELD_TYPE, episode.getType() );
                values.put( EpisodeConstants.FIELD_PUBLIC, episode.isNotVip() ? 0 : 1 );
                values.put( EpisodeConstants.FIELD_POSTED, episode.getPostedDate() );
                values.put( EpisodeConstants.FIELD_TIMESTAMP, episode.getTimestamp() );
                values.put( EpisodeConstants.FIELD_SHOWNAMEID, episode.getShowNameId() );
                values.put( EpisodeConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                Cursor cursor = provider.query( ContentUris.withAppendedId( EpisodeConstants.CONTENT_URI, episode.getShowId() ), projection, null, null, null );
                if( cursor.moveToFirst() ) {
                    //Log.v( TAG, "processEpisodes : episode iteration, updating existing entry" );

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
                    //Log.v( TAG, "processEpisodes : episode iteration, adding new entry" );

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

                //Log.v( TAG, "processEpisodes : processing guests" );
                if( null != episode.getGuests() && episode.getGuests().length > 0 ) {

                    for( Guest guest : episode.getGuests() ) {

                        //Log.v( TAG, "processEpisodes : guest=" + guest.toString() );

                        values = new ContentValues();
                        values.put( GuestConstants._ID, guest.getShowGuestId() );
                        values.put( GuestConstants.FIELD_REALNAME, guest.getRealName() );
                        values.put( GuestConstants.FIELD_DESCRIPTION, guest.getDescription() );
                        values.put( GuestConstants.FIELD_PICTUREFILENAME, guest.getPictureFilename() );
                        values.put( GuestConstants.FIELD_URL1, guest.getUrl1() );
                        values.put( GuestConstants.FIELD_URL2, guest.getUrl2() );
                        values.put( GuestConstants.FIELD_PICTUREURL, guest.getPictureUrl() );
                        values.put( GuestConstants.FIELD_PICTUREURLLARGE, guest.getPictureUrlLarge() );
                        values.put( GuestConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                        cursor = provider.query( ContentUris.withAppendedId( GuestConstants.CONTENT_URI, guest.getShowGuestId() ), null, null, null, null );
                        if( cursor.moveToFirst() ) {
                            //Log.v( TAG, "processEpisodes : guest iteration, updating existing entry" );

                            Long id = cursor.getLong(cursor.getColumnIndexOrThrow( GuestConstants._ID ) );
                            ops.add(
                                    ContentProviderOperation.newUpdate( ContentUris.withAppendedId( GuestConstants.CONTENT_URI, id ) )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );

                        } else {
                            //Log.v( TAG, "processEpisodes : guest iteration, adding new entry" );

                            ops.add(
                                    ContentProviderOperation.newInsert( GuestConstants.CONTENT_URI )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );
                        }
                        cursor.close();

                        values = new ContentValues();
                        values.put( EpisodeGuestConstants.FIELD_SHOWID, episode.getShowId() );
                        values.put( EpisodeGuestConstants.FIELD_SHOWGUESTID, guest.getShowGuestId() );
                        values.put( GuestConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                        cursor = provider.query( EpisodeGuestConstants.CONTENT_URI, null, EpisodeGuestConstants.FIELD_SHOWID + "=? and " + EpisodeGuestConstants.FIELD_SHOWGUESTID + "=?", new String[] { String.valueOf( episode.getShowId() ), String.valueOf( guest.getShowGuestId() ) }, null );
                        if( cursor.moveToFirst() ) {
                            //Log.v( TAG, "processEpisodes : episodeGuest iteration, updating existing entry" );

                            Long id = cursor.getLong( cursor.getColumnIndexOrThrow( EpisodeGuestConstants._ID ) );
                            ops.add(
                                    ContentProviderOperation.newUpdate( ContentUris.withAppendedId( EpisodeGuestConstants.CONTENT_URI, id ) )
                                            .withValues( values )
                                            .withYieldAllowed( true )
                                            .build()
                            );

                        } else {
                            //Log.v( TAG, "processEpisodes : episodeGuest iteration, adding new entry" );

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
                //Log.v( TAG, "processEpisodes : applying final batch for transactions" );

                ContentProviderResult[] results = provider.applyBatch( ops );
                loaded += results.length;

                if( results.length > 0 ) {
                    ops.clear();
                }
            }

            if( EndpointConstants.Type.RECENT.equals( type ) ) {
                if( !detailsQueue.isEmpty() ) {
                    //Log.v( TAG, "processEpisodes : processing show details" );

                    for( int showId : detailsQueue ) {
                        getShowDetails( provider, showId );
                    }
                }
            }

            //Log.i(TAG, "processEpisodes : episodes loaded '" + loaded + "'");

        } catch( Exception e ) {
            //Log.e(TAG, "processEpisodes : error", e);
        }

        //Log.v( TAG, "processEpisodes : exit" );
    }

    private void processEpisodeDetails( Detail detail, ContentProviderClient provider, int showId ) {
        //Log.v( TAG, "processEpisodeDetails : enter" );

        try {
            int count = 0, loaded = 0;

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] projection = new String[] { DetailConstants._ID };

            ContentValues values = new ContentValues();
            values.put( DetailConstants.FIELD_NOTES, detail.getNotes() );
            values.put( DetailConstants.FIELD_FORUMURL, detail.getForumUrl() );
            values.put( DetailConstants.FIELD_SHOWID, showId );
            values.put( DetailConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

            Cursor cursor = provider.query( DetailConstants.CONTENT_URI, projection, DetailConstants.FIELD_SHOWID + "=?", new String[] { String.valueOf( showId ) }, null );
            if( cursor.moveToFirst() ) {
                //Log.v( TAG, "processEpisodeDetails : detail iteration, updating existing entry" );

                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( DetailConstants._ID ) );
                ops.add(
                        ContentProviderOperation.newUpdate( ContentUris.withAppendedId( DetailConstants.CONTENT_URI, id ) )
                                .withValues( values )
                                .withYieldAllowed( true )
                                .build()
                );
            } else {
                //Log.v( TAG, "processEpisodeDetails : detail iteration, adding new entry" );

                ops.add(
                        ContentProviderOperation.newInsert( DetailConstants.CONTENT_URI )
                                .withValues( values )
                                .withYieldAllowed( true )
                                .build()
                );
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
                values.put( ImageConstants.FIELD_SHOWID, showId );
                values.put( ImageConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

                cursor = provider.query( ContentUris.withAppendedId( ImageConstants.CONTENT_URI, image.getPictureId() ), null, null, null, null );
                if( cursor.moveToFirst() ) {
                    //Log.v( TAG, "processEpisodeDetails : image iteration, updating existing entry" );

                    Long id = cursor.getLong( cursor.getColumnIndexOrThrow( ImageConstants._ID ) );
                    ops.add(
                            ContentProviderOperation.newUpdate( ContentUris.withAppendedId( ImageConstants.CONTENT_URI, id ) )
                                    .withValues( values )
                                    .withYieldAllowed( true )
                                    .build()
                    );
                } else {
                    //Log.v( TAG, "processEpisodeDetails : image iteration, adding new entry" );

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
                    //Log.v( TAG, "processEpisodeDetails : applying batch for '" + count + "' transactions" );

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
                //Log.v( TAG, "processEpisodeDetails : applying final batch for '" + count + "' transactions" );

                ContentProviderResult[] results = provider.applyBatch( ops );
                loaded += results.length;

                if( results.length > 0 ) {
                    ops.clear();
                }
            }

            //Log.i( TAG, "processEpisodeDetails : details loaded '" + loaded + "'" );

        } catch( Exception e ) {
            //Log.e( TAG, "processEpisodeDetails : error", e );
        }

        //Log.v( TAG, "processEpisodeDetails : exit" );
    }

    private void processYoutubeEpisodes( Youtube youtube, ContentProviderClient provider, Job job ) throws RemoteException, OperationApplicationException, JSONException {
        //Log.v( TAG, "processYoutubeEpisodes : enter" );

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

            if( null != youtube.getFeed() ) {

                if( null != youtube.getFeed().getEntries() && youtube.getFeed().getEntries().length > 0 ) {

                    for( YoutubeEntry entry : youtube.getFeed().getEntries() ) {

                        //if( null != json ) {

                            String content = "", thumbnail = "";
                            try {
                                content = entry.getContent().getContent();
                            } catch( Exception e ) {
                                //Log.v( TAG, "processYoutubeEpisodes : content is not valid" );
                            }

                            if( !"".equals( content ) ) {
                                String img = content.substring( content.indexOf( "<img" ) );
                                img = img.substring( 0, img.indexOf( "</a>" ) - 2 );

                                thumbnail = img.substring( img.indexOf( "src=" ) + 5 );
                            }

                            String etag = "";
                            try {
                                etag = entry.getEtag();
                            } catch( Exception e ) {
                                //Log.v( TAG, "processYoutubeEpisodes : etag is not valid" );
                            }

                            String youtubeId = "";
                            try {
                                youtubeId = entry.getId().getValue();
                            } catch( Exception e ) {
                                //Log.v( TAG, "processYoutubeEpisodes : id is not valid" );
                            } finally {
                                if( !"".equals( youtubeId ) ) {
                                    youtubeId = youtubeId.substring( youtubeId.lastIndexOf( ':' ) + 1 );
                                }
                            }

                            DateTime published = new DateTime();
                            try {
                                published = new DateTime( entry.getPublished() );
                            } catch( Exception e ) {
                                //Log.v( TAG, "processYoutubeEpisodes : published is not valid" );
                            } finally {
                                published = published.withZone( DateTimeZone.UTC );
                            }

                            DateTime updated = new DateTime();
                            try {
                                updated = new DateTime( entry.getUpdated() );
                            } catch( Exception e ) {
                                //Log.v( TAG, "processYoutubeEpisodes : updated is not valid" );
                            } finally {
                                updated = updated.withZone( DateTimeZone.UTC );
                            }

                            String title = "";
                            try {
                                title = entry.getTitle().getValue();
                            } catch( Exception e ) {
                                //Log.v( TAG, "processYoutubeEpisodes : title is not valid" );
                            }

                            String link = "";
                            if( null != entry.getLinks() && entry.getLinks().length > 0 ) {

                                for( YoutubeLink youtubeLink : entry.getLinks() ) {

                                    if( "alternate".equals( youtubeLink.getRel() ) ) {
                                        link = youtubeLink.getHref();
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
                                //Log.v( TAG, "processYoutubeEpisodes : updating existing entry" );

                                Long id = cursor.getLong( cursor.getColumnIndexOrThrow( YoutubeConstants._ID ) );
                                ops.add(
                                        ContentProviderOperation.newUpdate( ContentUris.withAppendedId( YoutubeConstants.CONTENT_URI, id ) )
                                                .withValues( values )
                                                .withYieldAllowed( true )
                                                .build()
                                );
                            } else {
                                //Log.v( TAG, "processYoutubeEpisodes : adding new entry" );

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
                                //Log.v( TAG, "processYoutubeEpisodes : applying batch for '" + count + "' transactions" );

                                if( !ops.isEmpty() ) {

                                    ContentProviderResult[] results = provider.applyBatch( ops );
                                    loaded += results.length;

                                    if( results.length > 0 ) {
                                        ops.clear();
                                    }
                                }
                                count = 0;
                            }
                        //}
                    }
                }
            }

            if( !ops.isEmpty() ) {
                //Log.v( TAG, "processYoutubeEpisodes : applying final batch for '" + count + "' transactions" );

                ContentProviderResult[] results = provider.applyBatch( ops );
                loaded += results.length;

                if( results.length > 0 ) {
                    ops.clear();
                }
            }

            mContext.getContentResolver().delete( YoutubeConstants.CONTENT_URI, YoutubeConstants.FIELD_LAST_MODIFIED_DATE + " != ?", new String[] { String.valueOf( now.getMillis() ) } );

            //Log.i( TAG, "processYoutubeEpisodes : events loaded '" + loaded + "'" );

            update.put( WorkItemConstants.FIELD_ETAG, job.getEtag() );
            update.put( WorkItemConstants.FIELD_LAST_RUN, lastRun.getMillis() );
            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.OK.name() );

        } catch( Exception e ) {
            //Log.e( TAG, "processYoutubeEpisodes : error", e );

            update.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.FAILED.name() );
        } finally {
            provider.update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, job.getId() ), update, null, null );
        }

        //Log.v( TAG, "processYoutubeEpisodes : exit" );
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
