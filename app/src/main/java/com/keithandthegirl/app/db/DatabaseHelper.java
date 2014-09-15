package com.keithandthegirl.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

/**
 * Created by dmfrey on 3/18/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "katgdb";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onOpen( SQLiteDatabase db ) {
        super.onOpen( db );

        if( !db.isReadOnly() ) {
            db.execSQL( "PRAGMA foreign_keys = ON;" );
        }
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
        dropTables(db);

        createTableWorkItems( db );
        createTableEndpoints( db );
        createTableShows( db );
        createTableLive( db );
        createTableEvents( db );
        createTableGuests( db );
        createTableEpisodes( db );
        createTableEpisodeDetails( db );
        createTableEpisodeDetailImages( db );
        createTableEpisodeGuests( db );
        createTableYoutube( db );
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {

        if( oldVersion < DATABASE_VERSION ) {
            Log.v(TAG, "onUpgrade : upgrading to db version " + DATABASE_VERSION);

            db.execSQL( WorkItemConstants.DROP_TABLE );
            createTableWorkItems( db );
        }

    }

    private void dropTables( SQLiteDatabase db ) {
        String dropWorkItem = WorkItemConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropWorkItem=" + dropWorkItem );
        }
        db.execSQL( dropWorkItem );

        String dropEndpoint = EndpointConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEndpoint=" + dropEndpoint );
        }
        db.execSQL( dropEndpoint );

        String dropLive = LiveConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropLive=" + dropLive );
        }
        db.execSQL( dropLive );

        String dropEvent = EventConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEvent=" + dropEvent );
        }
        db.execSQL( dropEvent );

        String dropEpisodeGuests = EpisodeGuestConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEpisodeGuests=" + dropEpisodeGuests );
        }
        db.execSQL( dropEpisodeGuests );

        String dropGuest = GuestConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropGuest=" + dropGuest );
        }
        db.execSQL( dropGuest );

        String dropEpisodeDetailImage = ImageConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEpisodeDetailImage=" + dropEpisodeDetailImage );
        }
        db.execSQL( dropEpisodeDetailImage );

        String dropEpisodeDetail = DetailConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEpisodeDetail=" + dropEpisodeDetail );
        }
        db.execSQL( dropEpisodeDetail );

        String dropEpisode = EpisodeConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEpisode=" + dropEpisode );
        }
        db.execSQL( dropEpisode );

        String dropShow = ShowConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropShow=" + dropShow );
        }
        db.execSQL( dropShow );

        String dropYoutube = YoutubeConstants.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropYoutube=" + dropYoutube );
        }
        db.execSQL( dropYoutube );
    }

    private void createTableEndpoints( SQLiteDatabase db ) {
        String sql = EndpointConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEndpoints : sql=" + sql );
        }
        db.execSQL( sql );

        DateTime now = new DateTime( DateTimeZone.UTC );
        db.execSQL( EndpointConstants.INSERT_ROW, new Object[] { EndpointConstants.Type.EVENTS.name(), EndpointConstants.EVENTS, EndpointConstants.DownloadType.ARRAY.name(), "", now.getMillis() } );
        db.execSQL( EndpointConstants.INSERT_ROW, new Object[] { EndpointConstants.Type.LIVE.name(), EndpointConstants.LIVE, EndpointConstants.DownloadType.OBJECT.name(), "", now.getMillis() } );
        db.execSQL( EndpointConstants.INSERT_ROW, new Object[] { EndpointConstants.Type.DETAILS.name(), EndpointConstants.DETAILS, EndpointConstants.DownloadType.OBJECT.name(), "", now.getMillis() } );
        db.execSQL( EndpointConstants.INSERT_ROW, new Object[] { EndpointConstants.Type.RECENT.name(), EndpointConstants.RECENT, EndpointConstants.DownloadType.ARRAY.name(), "", now.getMillis() } );
        db.execSQL( EndpointConstants.INSERT_ROW, new Object[] { EndpointConstants.Type.OVERVIEW.name(), EndpointConstants.OVERVIEW, EndpointConstants.DownloadType.ARRAY.name(), "", now.getMillis() } );
        db.execSQL( EndpointConstants.INSERT_ROW, new Object[] { EndpointConstants.Type.LIST.name(), EndpointConstants.LIST, EndpointConstants.DownloadType.ARRAY.name(), "", now.getMillis() } );
        db.execSQL( EndpointConstants.INSERT_ROW, new Object[] { EndpointConstants.Type.YOUTUBE.name(), EndpointConstants.YOUTUBE, EndpointConstants.DownloadType.OBJECT.name(), "", now.getMillis() } );
    }

    private void createTableWorkItems( SQLiteDatabase db ) {
        String sql = WorkItemConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableWorkItems : sql=" + sql );
        }
        db.execSQL(sql);

        DateTime now = new DateTime( DateTimeZone.UTC );
        db.execSQL( WorkItemConstants.INSERT_ROW, new Object[] { "Refresh Shows", WorkItemConstants.Frequency.DAILY, WorkItemConstants.Download.JSONARRAY, EndpointConstants.Type.OVERVIEW.name(), EndpointConstants.OVERVIEW, "", "", -1, WorkItemConstants.Status.NEVER.name(), now.getMillis() } );
        db.execSQL( WorkItemConstants.INSERT_ROW, new Object[] { "Refresh Events", WorkItemConstants.Frequency.DAILY, WorkItemConstants.Download.JSON, EndpointConstants.Type.EVENTS.name(), EndpointConstants.EVENTS, "", "", -1, WorkItemConstants.Status.NEVER.name(), now.getMillis() } );
        db.execSQL( WorkItemConstants.INSERT_ROW, new Object[] { "Refresh Broadcasting", WorkItemConstants.Frequency.HOURLY, WorkItemConstants.Download.JSON, EndpointConstants.Type.LIVE.name(), EndpointConstants.LIVE, "", "", -1, WorkItemConstants.Status.NEVER.name(), now.getMillis() } );
//        db.execSQL( WorkItemConstants.INSERT_ROW, new Object[] { "Refresh Recent Episodes", WorkItemConstants.Frequency.HOURLY, WorkItemConstants.Download.JSONARRAY, EndpointConstants.Type.RECENT.name(), EndpointConstants.RECENT, "", "", -1, WorkItemConstants.Status.NEVER.name(), now.getMillis() } );
        db.execSQL( WorkItemConstants.INSERT_ROW, new Object[] { "Refresh Youtube Episodes", WorkItemConstants.Frequency.DAILY, WorkItemConstants.Download.JSON, EndpointConstants.Type.YOUTUBE.name(), EndpointConstants.YOUTUBE, "", "", -1, WorkItemConstants.Status.NEVER.name(), now.getMillis() } );
    }

    private void createTableShows( SQLiteDatabase db ) {
        String sql = ShowConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableShows : sql=" + sql );
        }
        db.execSQL( sql );
    }

    private void createTableEpisodes( SQLiteDatabase db ) {
        String sql = EpisodeConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEpisodes : sql=" + sql );
        }
        db.execSQL(sql);
    }

    private void createTableEpisodeDetails( SQLiteDatabase db ) {
        String sql = DetailConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEpisodeDetails : sql=" + sql );
        }
        db.execSQL( sql );
    }

    private void createTableEpisodeDetailImages( SQLiteDatabase db ) {
        String sql = ImageConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEpisodeDetailImages : sql=" + sql );
        }
        db.execSQL( sql );
    }

    private void createTableGuests( SQLiteDatabase db ) {
        String sql = GuestConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableGuests : sql=" + sql );
        }
        db.execSQL( sql );
    }

    private void createTableEpisodeGuests( SQLiteDatabase db ) {
        String sql = EpisodeGuestConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEpisodeGuests : sql=" + sql );
        }
        db.execSQL( sql );
    }

    private void createTableEvents( SQLiteDatabase db ) {
        String sql = EventConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEvents : sql=" + sql );
        }
        db.execSQL( sql );
    }

    private void createTableLive( SQLiteDatabase db ) {
        String sql = LiveConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableLive : sql=" + sql );
        }
        db.execSQL( sql );

        DateTime now = new DateTime( DateTimeZone.UTC );
        db.execSQL( LiveConstants.INSERT_ROW, new Object[] { "1", "0", now.getMillis() } );
    }

    private void createTableYoutube( SQLiteDatabase db ) {
        String sql = YoutubeConstants.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableYoutube : sql=" + sql );
        }
        db.execSQL( sql );
    }
}