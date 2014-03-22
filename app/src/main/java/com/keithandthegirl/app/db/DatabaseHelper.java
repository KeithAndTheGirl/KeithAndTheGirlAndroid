package com.keithandthegirl.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

/**
 * Created by dmfrey on 3/18/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "katgdb";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onOpen( SQLiteDatabase db ) {
        Log.v( TAG, "onOpen : enter" );
        super.onOpen( db );

        if( !db.isReadOnly() ) {
            Log.i( TAG, "onOpen : turning on referencial integrity" );

            db.execSQL( "PRAGMA foreign_keys = ON;" );
        }

        Log.v(TAG, "onOpen : exit");
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
        Log.d(TAG, "onCreate : enter");

        dropTables(db);

        createTableEndpoints( db );
        createTableShows( db );
        createTableLive( db );
        createTableEvents( db );
        createTableGuests( db );
        createTableEpisodes( db );
        createTableEpisodeDetails( db );
        createTableEpisodeDetailImages( db );
        createTableEpisodeGuests( db );

        Log.d(TAG, "onCreate : exit");
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        Log.d( TAG, "onCreate : enter" );

        if( oldVersion < DATABASE_VERSION ) {
            Log.v(TAG, "onUpgrade : upgrading to db version " + DATABASE_VERSION);

        }


        Log.d( TAG, "onCreate : exit" );
    }

    private void dropTables( SQLiteDatabase db ) {
        Log.v( TAG, "dropTables : enter" );

        String dropWorkItem = WorkItem.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropWorkItem=" + dropWorkItem );
        }
        db.execSQL( dropWorkItem );

        String dropEndpoint = Endpoint.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEndpoint=" + dropEndpoint );
        }
        db.execSQL( dropEndpoint );

        String dropLive = Live.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropLive=" + dropLive );
        }
        db.execSQL( dropLive );

        String dropEvent = Event.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEvent=" + dropEvent );
        }
        db.execSQL( dropEvent );

        String dropEpisodeGuests = EpisodeGuests.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEpisodeGuests=" + dropEpisodeGuests );
        }
        db.execSQL( dropEpisodeGuests );

        String dropGuest = Guest.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropGuest=" + dropGuest );
        }
        db.execSQL( dropGuest );

        String dropEpisodeDetailImage = Image.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEpisodeDetailImage=" + dropEpisodeDetailImage );
        }
        db.execSQL( dropEpisodeDetailImage );

        String dropEpisodeDetail = Detail.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEpisodeDetail=" + dropEpisodeDetail );
        }
        db.execSQL( dropEpisodeDetail );

        String dropEpisode = Episode.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropEpisode=" + dropEpisode );
        }
        db.execSQL( dropEpisode );

        String dropShow = Show.DROP_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "dropTable : dropShow=" + dropShow );
        }
        db.execSQL( dropShow );

        Log.v( TAG, "dropTables : exit" );
    }

    private void createTableEndpoints( SQLiteDatabase db ) {
        Log.v( TAG, "createTableEndpoints : enter" );

        String sql = Endpoint.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEndpoints : sql=" + sql );
        }
        db.execSQL( sql );

        DateTime now = new DateTime( DateTimeZone.UTC );
        db.execSQL( Endpoint.INSERT_ROW, new Object[] { Endpoint.Type.EVENTS.name(), Endpoint.EVENTS, Endpoint.DownloadType.ARRAY.name(), "", now.getMillis() } );
        db.execSQL( Endpoint.INSERT_ROW, new Object[] { Endpoint.Type.LIVE.name(), Endpoint.LIVE, Endpoint.DownloadType.OBJECT.name(), "", now.getMillis() } );
        db.execSQL( Endpoint.INSERT_ROW, new Object[] { Endpoint.Type.DETAILS.name(), Endpoint.DETAILS, Endpoint.DownloadType.OBJECT.name(), "", now.getMillis() } );
        db.execSQL( Endpoint.INSERT_ROW, new Object[] { Endpoint.Type.RECENT.name(), Endpoint.RECENT, Endpoint.DownloadType.ARRAY.name(), "", now.getMillis() } );
        db.execSQL( Endpoint.INSERT_ROW, new Object[] { Endpoint.Type.OVERVIEW.name(), Endpoint.OVERVIEW, Endpoint.DownloadType.ARRAY.name(), "", now.getMillis() } );
        db.execSQL( Endpoint.INSERT_ROW, new Object[] { Endpoint.Type.LIST.name(), Endpoint.LIST, Endpoint.DownloadType.ARRAY.name(), "", now.getMillis() } );

        Log.v( TAG, "createTableEndpoints : exit" );
    }

    private void createTableWorkItems( SQLiteDatabase db ) {
        Log.v( TAG, "createTableWorkItems : enter" );

        String sql = WorkItem.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableWorkItems : sql=" + sql );
        }
        db.execSQL(sql);

        Log.v( TAG, "createTableWorkItems : exit" );
    }

    private void createTableShows( SQLiteDatabase db ) {
        Log.v( TAG, "createTableShows : enter" );

        String sql = Show.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableShows : sql=" + sql );
        }
        db.execSQL( sql );

        Log.v( TAG, "createTableShows : exit" );
    }

    private void createTableEpisodes( SQLiteDatabase db ) {
        Log.v( TAG, "createTableEpisodes : enter" );

        String sql = Episode.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEpisodes : sql=" + sql );
        }
        db.execSQL(sql);

        Log.v( TAG, "createTableEpisodes : exit" );
    }

    private void createTableEpisodeDetails( SQLiteDatabase db ) {
        Log.v( TAG, "createTableEpisodeDetails : enter" );

        String sql = Detail.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEpisodeDetails : sql=" + sql );
        }
        db.execSQL( sql );

        Log.v( TAG, "createTableEpisodeDetails : exit" );
    }

    private void createTableEpisodeDetailImages( SQLiteDatabase db ) {
        Log.v( TAG, "createTableEpisodeDetailImages : enter" );

        String sql = Image.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEpisodeDetailImages : sql=" + sql );
        }
        db.execSQL( sql );

        Log.v( TAG, "createTableEpisodeDetailImages : exit" );
    }

    private void createTableGuests( SQLiteDatabase db ) {
        Log.v( TAG, "createTableGuests : enter" );

        String sql = Guest.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableGuests : sql=" + sql );
        }
        db.execSQL( sql );


        Log.v( TAG, "createTableGuests : exit" );
    }

    private void createTableEpisodeGuests( SQLiteDatabase db ) {
        Log.v( TAG, "createTableEpisodeGuests : enter" );

        String sql = EpisodeGuests.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEpisodeGuests : sql=" + sql );
        }
        db.execSQL( sql );


        Log.v( TAG, "createTableEpisodeGuests : exit" );
    }

    private void createTableEvents( SQLiteDatabase db ) {
        Log.v( TAG, "createTableEvents : enter" );

        String sql = Event.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableEvents : sql=" + sql );
        }
        db.execSQL( sql );

        Log.v( TAG, "createTableEvents : exit" );
    }

    private void createTableLive( SQLiteDatabase db ) {
        Log.v( TAG, "createTableLive : enter" );

        String sql = Live.CREATE_TABLE;
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createTableLive : sql=" + sql );
        }
        db.execSQL( sql );

        DateTime now = new DateTime( DateTimeZone.UTC );
        db.execSQL( Live.INSERT_ROW, new Object[] { "1", "0", now.getMillis() } );

        Log.v(TAG, "createTableLive : exit");
    }

}
