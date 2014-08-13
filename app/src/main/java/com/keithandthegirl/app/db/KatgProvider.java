package com.keithandthegirl.app.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
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

import java.util.ArrayList;

import static android.provider.BaseColumns._ID;

/**
 * Created by dmfrey on 3/19/14.
 */
public class KatgProvider extends ContentProvider {

    private static final String TAG = KatgProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.keithandthegirl.provider";

    private static final UriMatcher URI_MATCHER;
    private DatabaseHelper database = null;

    static {

        URI_MATCHER = new UriMatcher( UriMatcher.NO_MATCH );

        URI_MATCHER.addURI( AUTHORITY, EndpointConstants.TABLE_NAME, EndpointConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, EndpointConstants.TABLE_NAME + "/#",  EndpointConstants.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, ShowConstants.TABLE_NAME, ShowConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, ShowConstants.TABLE_NAME + "/#",  ShowConstants.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, EventConstants.TABLE_NAME, EventConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, EventConstants.TABLE_NAME + "/#",  EventConstants.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, LiveConstants.TABLE_NAME, LiveConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, LiveConstants.TABLE_NAME + "/#",  LiveConstants.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, GuestConstants.TABLE_NAME, GuestConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, GuestConstants.TABLE_NAME + "/#",  GuestConstants.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, EpisodeConstants.TABLE_NAME, EpisodeConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, EpisodeConstants.TABLE_NAME + "/#",  EpisodeConstants.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, DetailConstants.TABLE_NAME, DetailConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, DetailConstants.TABLE_NAME + "/#",  DetailConstants.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, ImageConstants.TABLE_NAME, ImageConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, ImageConstants.TABLE_NAME + "/#",  ImageConstants.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, EpisodeGuestConstants.TABLE_NAME, EpisodeGuestConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, EpisodeGuestConstants.TABLE_NAME + "/#",  EpisodeGuestConstants.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, YoutubeConstants.TABLE_NAME, YoutubeConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, YoutubeConstants.TABLE_NAME + "/#",  YoutubeConstants.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, WorkItemConstants.TABLE_NAME, WorkItemConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, WorkItemConstants.TABLE_NAME + "/#",  WorkItemConstants.SINGLE );

    }

    @Override
    public boolean onCreate() {
//		Log.v( TAG, "onCreate : enter" );

        database = new DatabaseHelper( getContext() );

//		Log.v( TAG, "onCreate : exit" );
        return ( null == database ? false : true );
    }

    @Override
    public String getType( Uri uri ) {
//        Log.v( TAG, "getType : enter" );

        switch( URI_MATCHER.match( uri ) ) {

            case EndpointConstants.ALL :
                return EndpointConstants.CONTENT_TYPE;

            case EndpointConstants.SINGLE :
                return EndpointConstants.CONTENT_ITEM_TYPE;

            case ShowConstants.ALL :
                return ShowConstants.CONTENT_TYPE;

            case ShowConstants.SINGLE :
                return ShowConstants.CONTENT_ITEM_TYPE;

            case EventConstants.ALL :
                return EventConstants.CONTENT_TYPE;

            case EventConstants.SINGLE :
                return EventConstants.CONTENT_ITEM_TYPE;

            case LiveConstants.ALL :
                return LiveConstants.CONTENT_TYPE;

            case LiveConstants.SINGLE :
                return LiveConstants.CONTENT_ITEM_TYPE;

            case GuestConstants.ALL :
                return GuestConstants.CONTENT_TYPE;

            case GuestConstants.SINGLE :
                return GuestConstants.CONTENT_ITEM_TYPE;

            case EpisodeConstants.ALL :
                return EpisodeConstants.CONTENT_TYPE;

            case EpisodeConstants.SINGLE :
                return EpisodeConstants.CONTENT_ITEM_TYPE;

            case DetailConstants.ALL :
                return DetailConstants.CONTENT_TYPE;

            case DetailConstants.SINGLE :
                return DetailConstants.CONTENT_ITEM_TYPE;

            case ImageConstants.ALL :
                return ImageConstants.CONTENT_TYPE;

            case ImageConstants.SINGLE :
                return ImageConstants.CONTENT_ITEM_TYPE;

            case EpisodeGuestConstants.ALL :
                return EpisodeGuestConstants.CONTENT_TYPE;

            case EpisodeGuestConstants.SINGLE :
                return EpisodeGuestConstants.CONTENT_ITEM_TYPE;

            case YoutubeConstants.ALL :
                return YoutubeConstants.CONTENT_TYPE;

            case YoutubeConstants.SINGLE :
                return YoutubeConstants.CONTENT_ITEM_TYPE;

            case WorkItemConstants.ALL :
                return WorkItemConstants.CONTENT_TYPE;

            case WorkItemConstants.SINGLE :
                return WorkItemConstants.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder ) {
//        Log.v( TAG, "query : enter" );

        final SQLiteDatabase db = database.getReadableDatabase();

        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        StringBuilder sb = new StringBuilder();

        Cursor cursor = null;

        switch( URI_MATCHER.match( uri ) ) {

            case EndpointConstants.ALL :

                cursor = db.query( EndpointConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case EndpointConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( EndpointConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case ShowConstants.ALL :

                cursor = db.query( ShowConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case ShowConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( ShowConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case EventConstants.ALL :

                cursor = db.query( EventConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case EventConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( EventConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case LiveConstants.ALL :

                cursor = db.query( LiveConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case LiveConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( LiveConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case GuestConstants.ALL :

                cursor = db.query( GuestConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case GuestConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( GuestConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case EpisodeConstants.ALL :

                cursor = db.query( EpisodeConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case EpisodeConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( EpisodeConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case DetailConstants.ALL :

                cursor = db.query( DetailConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case DetailConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( DetailConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case ImageConstants.ALL :

                cursor = db.query( ImageConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case ImageConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( ImageConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case EpisodeGuestConstants.ALL :

                cursor = db.query( EpisodeGuestConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case EpisodeGuestConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( EpisodeGuestConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case YoutubeConstants.ALL :

                cursor = db.query( YoutubeConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case YoutubeConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( YoutubeConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case WorkItemConstants.ALL :

                cursor = db.query( WorkItemConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case WorkItemConstants.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( WorkItemConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public Uri insert( Uri uri, ContentValues values ) {
//        Log.v( TAG, "insert : enter" );

        final SQLiteDatabase db = database.getWritableDatabase();

        Uri newUri = null;

        switch( URI_MATCHER.match( uri ) ) {

            case ShowConstants.ALL:

                newUri = ContentUris.withAppendedId( ShowConstants.CONTENT_URI, db.insertWithOnConflict( ShowConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case EventConstants.ALL:

                newUri = ContentUris.withAppendedId( EventConstants.CONTENT_URI, db.insertWithOnConflict( EventConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case LiveConstants.ALL:

                newUri = ContentUris.withAppendedId( LiveConstants.CONTENT_URI, db.insertWithOnConflict( LiveConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case GuestConstants.ALL:

                newUri = ContentUris.withAppendedId( GuestConstants.CONTENT_URI, db.insertWithOnConflict( GuestConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case EpisodeConstants.ALL:

                newUri = ContentUris.withAppendedId( EpisodeConstants.CONTENT_URI, db.insertWithOnConflict( EpisodeConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case DetailConstants.ALL:

                newUri = ContentUris.withAppendedId( DetailConstants.CONTENT_URI, db.insertWithOnConflict( DetailConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case ImageConstants.ALL:

                newUri = ContentUris.withAppendedId( ImageConstants.CONTENT_URI, db.insertWithOnConflict( ImageConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case EpisodeGuestConstants.ALL:

                newUri = ContentUris.withAppendedId( EpisodeGuestConstants.CONTENT_URI, db.insertWithOnConflict( EpisodeGuestConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case YoutubeConstants.ALL:

                newUri = ContentUris.withAppendedId( YoutubeConstants.CONTENT_URI, db.insertWithOnConflict( YoutubeConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case WorkItemConstants.ALL:

                newUri = ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, db.insertWithOnConflict( WorkItemConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs ) {
//        Log.v( TAG, "delete : enter" );

        final SQLiteDatabase db = database.getWritableDatabase();

        int deleted = 0;

        switch( URI_MATCHER.match( uri ) ) {

            case ShowConstants.ALL:

                deleted = db.delete( ShowConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case ShowConstants.SINGLE:

                deleted = db.delete( ShowConstants.TABLE_NAME, ShowConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case EventConstants.ALL:

                deleted = db.delete( EventConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case EventConstants.SINGLE:

                deleted = db.delete( EventConstants.TABLE_NAME, EventConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case LiveConstants.ALL:

                deleted = db.delete( LiveConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case LiveConstants.SINGLE:

                deleted = db.delete( LiveConstants.TABLE_NAME, LiveConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case GuestConstants.ALL:

                deleted = db.delete( GuestConstants.TABLE_NAME, selection, selectionArgs );

                //TODO: Need to delete all from EpisodeConstants Guests table

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case GuestConstants.SINGLE:

                deleted = db.delete( GuestConstants.TABLE_NAME, GuestConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                //TODO: Need to delete select entries from EpisodeConstants Guests table

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case EpisodeConstants.ALL:

                //TODO: Need to delete all images on the filesystem

                deleted = db.delete( EpisodeConstants.TABLE_NAME, selection, selectionArgs );

                //TODO: Need to delete all from Details table

                //TODO: Need to delete all from Images table

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case EpisodeConstants.SINGLE:

                //TODO: Need to delete select images on the filesystem

                deleted = db.delete( EpisodeConstants.TABLE_NAME, LiveConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                //TODO: Need to delete select details from Details table

                //TODO: Need to delete select images from Images table

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case DetailConstants.ALL:

                deleted = db.delete( DetailConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case DetailConstants.SINGLE:

                deleted = db.delete( DetailConstants.TABLE_NAME, DetailConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case ImageConstants.ALL:

                deleted = db.delete( ImageConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case ImageConstants.SINGLE:

                deleted = db.delete( ImageConstants.TABLE_NAME, ImageConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case EpisodeGuestConstants.ALL:

                deleted = db.delete( EpisodeGuestConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case EpisodeGuestConstants.SINGLE:

                deleted = db.delete( EpisodeGuestConstants.TABLE_NAME, LiveConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case YoutubeConstants.ALL:

                deleted = db.delete( YoutubeConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case YoutubeConstants.SINGLE:

                deleted = db.delete( YoutubeConstants.TABLE_NAME, YoutubeConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case WorkItemConstants.ALL:

                deleted = db.delete( WorkItemConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case WorkItemConstants.SINGLE:

                deleted = db.delete( WorkItemConstants.TABLE_NAME, WorkItemConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs ) {
//        Log.v( TAG, "update : enter" );

        final SQLiteDatabase db = database.getWritableDatabase();

        int affected = 0;

        switch( URI_MATCHER.match( uri ) ) {

            case ShowConstants.ALL:

                affected = db.update( ShowConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case ShowConstants.SINGLE:

                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( ShowConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case EventConstants.ALL:

                affected = db.update( EventConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case EventConstants.SINGLE:

                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( EventConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case LiveConstants.ALL:

                affected = db.update( LiveConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case LiveConstants.SINGLE:

                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( LiveConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case GuestConstants.ALL:

                affected = db.update( GuestConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case GuestConstants.SINGLE:

                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( GuestConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case EpisodeConstants.ALL:

                affected = db.update( EpisodeConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case EpisodeConstants.SINGLE:

                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( EpisodeConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case DetailConstants.ALL:

                affected = db.update( DetailConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case DetailConstants.SINGLE:

                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( DetailConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case ImageConstants.ALL:

                affected = db.update( ImageConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case ImageConstants.SINGLE:

                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( ImageConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case EpisodeGuestConstants.ALL:

                affected = db.update( EpisodeGuestConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case EpisodeGuestConstants.SINGLE:

                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( EpisodeGuestConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case YoutubeConstants.ALL:

                affected = db.update( YoutubeConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case YoutubeConstants.SINGLE:

                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( YoutubeConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case WorkItemConstants.ALL:

                affected = db.update( WorkItemConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case WorkItemConstants.SINGLE:

                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( WorkItemConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public ContentProviderResult[] applyBatch( ArrayList<ContentProviderOperation> operations )	 throws OperationApplicationException {
//        Log.v( TAG, "applyBatch : enter" );

        final SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {

            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[ numOperations ];
            for( int i = 0; i < numOperations; i++ ) {
                results[ i ] = operations.get( i ).apply( this, results, i );
            }
            db.setTransactionSuccessful();

            return results;

        } finally {
            db.endTransaction();
        }

    }

    private String appendRowId( String selection, long id ) {
        return _ID
                + "="
                + id
                + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" );
    }

}
