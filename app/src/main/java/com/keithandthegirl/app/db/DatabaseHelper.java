package com.keithandthegirl.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.DetailConstants;
import com.keithandthegirl.app.db.model.EndpointConstants;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.EpisodeGuestConstants;
import com.keithandthegirl.app.db.model.Episodes;
import com.keithandthegirl.app.db.model.EventConstants;
import com.keithandthegirl.app.db.model.Guest;
import com.keithandthegirl.app.db.model.GuestConstants;
import com.keithandthegirl.app.db.model.ImageConstants;
import com.keithandthegirl.app.db.model.LiveConstants;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.db.model.Shows;
import com.keithandthegirl.app.db.model.WorkItemConstants;
import com.keithandthegirl.app.db.model.YoutubeConstants;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dmfrey on 3/18/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "katgdb";
    private static final int DATABASE_VERSION = 6;

    private Context mContext;

    public DatabaseHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );

        mContext = context;

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

        loadSeriesOverview( db );
        loadKatg( db );
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {

        if( oldVersion < DATABASE_VERSION ) {
            Log.v(TAG, "onUpgrade : upgrading to db version " + DATABASE_VERSION);

            onCreate( db );
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

    private void loadSeriesOverview( SQLiteDatabase db ) {
        Log.v( TAG, "loadSeriesOverview : enter" );

        Gson katgGson = new GsonBuilder()
            .setDateFormat( "MM/dd/yyyy HH:mm" )
            .create();

        InputStream json = null;
        BufferedReader reader = null;
        try {

            json = mContext.getResources().openRawResource( R.raw.katg_overview );
            reader = new BufferedReader( new InputStreamReader( json ) );

            ContentValues values;

            Show[] shows = katgGson.fromJson( reader, Show[].class );
            for( Show show : shows ) {

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

                db.insert( ShowConstants.TABLE_NAME, null, values );

            }

        } finally {

            try {

                if( null != reader ) {
                    reader.close();
                }

            } catch( IOException e ) {
                Log.e( TAG, "loadSeriesOverview : error closing reader", e );
            }

            try {

                if( null != json ) {
                    json.close();
                }

            } catch( IOException e ) {
                Log.e( TAG, "loadSeriesOverview : error closing json", e );
            }

        }

        Log.v( TAG, "loadSeriesOverview : exit" );
    }

    private void loadKatg( SQLiteDatabase db ) {
        Log.v( TAG, "loadKatg : enter" );

        Gson katgGson = new GsonBuilder()
                .setDateFormat( "MM/dd/yyyy HH:mm" )
                .create();

        InputStream json = null;
        BufferedReader reader = null;
        try {

            json = mContext.getResources().openRawResource( R.raw.katg );
            reader = new BufferedReader( new InputStreamReader( json ) );

            ContentValues values;

            Episode[] episodes = katgGson.fromJson( reader, Episode[].class );
            for( Episode episode : episodes ) {

                String fileName = "";
                try {

                    Uri fileUrl = Uri.parse(episode.getFileUrl());
                    if (null != fileUrl.getLastPathSegment()) {
                        fileName = fileUrl.getLastPathSegment();
                        //Log.v( TAG, "processEpisodes : fileName=" + fileName );
                    }

                } catch (NullPointerException e) {
                }

                values = new ContentValues();
                values.put(EpisodeConstants._ID, episode.getShowId());
                values.put(EpisodeConstants.FIELD_NUMBER, episode.getNumber());
                values.put(EpisodeConstants.FIELD_TITLE, episode.getTitle());
                values.put(EpisodeConstants.FIELD_VIDEOFILEURL, episode.getVideoFileUrl());
                values.put(EpisodeConstants.FIELD_VIDEOTHUMBNAILURL, episode.getVideoThumbnailUrl());
                values.put(EpisodeConstants.FIELD_PREVIEWURL, episode.getPreviewUrl());
                values.put(EpisodeConstants.FIELD_FILEURL, episode.getFileUrl());
                values.put(EpisodeConstants.FIELD_FILENAME, fileName);
                values.put(EpisodeConstants.FIELD_LENGTH, episode.getLength());
                values.put(EpisodeConstants.FIELD_FILESIZE, episode.getFileSize());
                values.put(EpisodeConstants.FIELD_TYPE, episode.getType());
                values.put(EpisodeConstants.FIELD_PUBLIC, episode.getVip());
                values.put(EpisodeConstants.FIELD_POSTED, episode.getPostedDate());
                values.put(EpisodeConstants.FIELD_TIMESTAMP, episode.getTimestamp());
                values.put(EpisodeConstants.FIELD_SHOWNAMEID, episode.getShowNameId());
                values.put(EpisodeConstants.FIELD_LAST_MODIFIED_DATE, new DateTime(DateTimeZone.UTC).getMillis());
                values.put(EpisodeConstants.FIELD_DOWNLOADED, -1);
                values.put(EpisodeConstants.FIELD_PLAYED, -1);
                values.put(EpisodeConstants.FIELD_LASTPLAYED, -1);

                long eposideId = db.insertWithOnConflict( EpisodeConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE );

                if (null != episode.getGuests() && episode.getGuests().length > 0) {

                    List<String> guestNames = new ArrayList<String>();
                    List<String> guestIds = new ArrayList<String>();
                    List<String> guestImages = new ArrayList<String>();

                    for (Guest guest : episode.getGuests()) {
                        Log.v(TAG, "onPostExecute : guest=" + guest.toString());

                        guestNames.add(guest.getRealName());
                        guestIds.add(String.valueOf(guest.getShowGuestId()));
                        guestImages.add(guest.getPictureUrlLarge());

                        values = new ContentValues();
                        values.put(GuestConstants._ID, guest.getShowGuestId());
                        values.put(GuestConstants.FIELD_REALNAME, guest.getRealName());
                        values.put(GuestConstants.FIELD_DESCRIPTION, guest.getDescription());
                        values.put(GuestConstants.FIELD_PICTUREFILENAME, guest.getPictureFilename());
                        values.put(GuestConstants.FIELD_URL1, guest.getUrl1());
                        values.put(GuestConstants.FIELD_URL2, guest.getUrl2());
                        values.put(GuestConstants.FIELD_PICTUREURL, guest.getPictureUrl());
                        values.put(GuestConstants.FIELD_PICTUREURLLARGE, guest.getPictureUrlLarge());
                        values.put(GuestConstants.FIELD_LAST_MODIFIED_DATE, new DateTime(DateTimeZone.UTC).getMillis());

                        db.insertWithOnConflict(GuestConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

                        values = new ContentValues();
                        values.put(EpisodeGuestConstants.FIELD_SHOWID, episode.getShowId());
                        values.put(EpisodeGuestConstants.FIELD_SHOWGUESTID, guest.getShowGuestId());
                        values.put(EpisodeGuestConstants.FIELD_LAST_MODIFIED_DATE, new DateTime(DateTimeZone.UTC).getMillis());

                        db.insertWithOnConflict( EpisodeGuestConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE );

                    }

//                    if (!guestNames.isEmpty()) {
//
//                        values = new ContentValues();
//                        values.put(EpisodeConstants.FIELD_GUEST_NAMES, concatList(guestNames, ","));
//                        values.put(EpisodeConstants.FIELD_GUEST_IDS, concatList(guestIds, ","));
//                        values.put(EpisodeConstants.FIELD_GUEST_IMAGES, concatList(guestImages, ","));
//
//                        db.insertWithOnConflict( EpisodeConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE );
//
//                    }

                }

            }

            values = new ContentValues();
            values.put(ShowConstants.FIELD_EPISODE_COUNT_NEW, episodes.length );

            db.update( ShowConstants.TABLE_NAME, values, ShowConstants._ID + " = ?", new String[] { "1" } );

        } finally {

            try {

                if( null != reader ) {
                    reader.close();
                }

            } catch( IOException e ) {
                Log.e( TAG, "loadKatg : error closing reader", e );
            }

            try {

                if( null != json ) {
                    json.close();
                }

            } catch( IOException e ) {
                Log.e( TAG, "loadKatg : error closing json", e );
            }

        }

        Log.v( TAG, "loadKatg : exit" );
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