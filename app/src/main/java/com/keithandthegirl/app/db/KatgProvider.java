package com.keithandthegirl.app.db;

import static android.provider.BaseColumns._ID;

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

import java.util.ArrayList;

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

        URI_MATCHER.addURI( AUTHORITY, Endpoint.TABLE_NAME, Endpoint.ALL );
        URI_MATCHER.addURI( AUTHORITY, Endpoint.TABLE_NAME + "/#",  Endpoint.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, Show.TABLE_NAME, Show.ALL );
        URI_MATCHER.addURI( AUTHORITY, Show.TABLE_NAME + "/#",  Show.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, Event.TABLE_NAME, Event.ALL );
        URI_MATCHER.addURI( AUTHORITY, Event.TABLE_NAME + "/#",  Event.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, Live.TABLE_NAME, Live.ALL );
        URI_MATCHER.addURI( AUTHORITY, Live.TABLE_NAME + "/#",  Live.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, Guest.TABLE_NAME, Guest.ALL );
        URI_MATCHER.addURI( AUTHORITY, Guest.TABLE_NAME + "/#",  Guest.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, Episode.TABLE_NAME, Episode.ALL );
        URI_MATCHER.addURI( AUTHORITY, Episode.TABLE_NAME + "/#",  Episode.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, Detail.TABLE_NAME, Detail.ALL );
        URI_MATCHER.addURI( AUTHORITY, Detail.TABLE_NAME + "/#",  Detail.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, Image.TABLE_NAME, Image.ALL );
        URI_MATCHER.addURI( AUTHORITY, Image.TABLE_NAME + "/#",  Image.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, EpisodeGuests.TABLE_NAME, EpisodeGuests.ALL );
        URI_MATCHER.addURI( AUTHORITY, EpisodeGuests.TABLE_NAME + "/#",  EpisodeGuests.SINGLE );

        URI_MATCHER.addURI( AUTHORITY, WorkItem.TABLE_NAME, WorkItem.ALL );
        URI_MATCHER.addURI( AUTHORITY, WorkItem.TABLE_NAME + "/#",  WorkItem.SINGLE );

    }

    @Override
    public boolean onCreate() {
		Log.v( TAG, "onCreate : enter" );

        database = new DatabaseHelper( getContext() );

		Log.v( TAG, "onCreate : exit" );
        return ( null == database ? false : true );
    }

    @Override
    public String getType( Uri uri ) {
        Log.v( TAG, "getType : enter" );

        switch( URI_MATCHER.match( uri ) ) {

            case Endpoint.ALL :
                return Endpoint.CONTENT_TYPE;

            case Endpoint.SINGLE :
                return Endpoint.CONTENT_ITEM_TYPE;

            case Show.ALL :
                return Show.CONTENT_TYPE;

            case Show.SINGLE :
                return Show.CONTENT_ITEM_TYPE;

            case Event.ALL :
                return Event.CONTENT_TYPE;

            case Event.SINGLE :
                return Event.CONTENT_ITEM_TYPE;

            case Live.ALL :
                return Live.CONTENT_TYPE;

            case Live.SINGLE :
                return Live.CONTENT_ITEM_TYPE;

            case Guest.ALL :
                return Guest.CONTENT_TYPE;

            case Guest.SINGLE :
                return Guest.CONTENT_ITEM_TYPE;

            case Episode.ALL :
                return Episode.CONTENT_TYPE;

            case Episode.SINGLE :
                return Episode.CONTENT_ITEM_TYPE;

            case Detail.ALL :
                return Detail.CONTENT_TYPE;

            case Detail.SINGLE :
                return Detail.CONTENT_ITEM_TYPE;

            case Image.ALL :
                return Image.CONTENT_TYPE;

            case Image.SINGLE :
                return Image.CONTENT_ITEM_TYPE;

            case EpisodeGuests.ALL :
                return EpisodeGuests.CONTENT_TYPE;

            case EpisodeGuests.SINGLE :
                return EpisodeGuests.CONTENT_ITEM_TYPE;

            case WorkItem.ALL :
                return WorkItem.CONTENT_TYPE;

            case WorkItem.SINGLE :
                return WorkItem.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder ) {
        Log.v( TAG, "query : enter" );

        final SQLiteDatabase db = database.getReadableDatabase();

        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        StringBuilder sb = new StringBuilder();

        Cursor cursor = null;

        switch( URI_MATCHER.match( uri ) ) {

            case Endpoint.ALL :

                cursor = db.query( Endpoint.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Endpoint.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( Endpoint.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Show.ALL :

                cursor = db.query( Show.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Show.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( Show.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Event.ALL :

                cursor = db.query( Event.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Event.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( Event.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Live.ALL :

                cursor = db.query( Live.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Live.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( Live.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Guest.ALL :

                cursor = db.query( Guest.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Guest.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( Guest.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Episode.ALL :

                cursor = db.query( Episode.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Episode.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( Episode.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Detail.ALL :

                cursor = db.query( Detail.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Detail.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( Detail.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Image.ALL :

                cursor = db.query( Image.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case Image.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( Image.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case EpisodeGuests.ALL :

                cursor = db.query( EpisodeGuests.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case EpisodeGuests.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( EpisodeGuests.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case WorkItem.ALL :

                cursor = db.query( WorkItem.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case WorkItem.SINGLE :
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( WorkItem.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public Uri insert( Uri uri, ContentValues values ) {
        Log.v( TAG, "insert : enter" );

        final SQLiteDatabase db = database.getWritableDatabase();

        Uri newUri = null;

        switch( URI_MATCHER.match( uri ) ) {

            case Show.ALL:

                newUri = ContentUris.withAppendedId( Show.CONTENT_URI, db.insertWithOnConflict( Show.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case Event.ALL:

                newUri = ContentUris.withAppendedId( Event.CONTENT_URI, db.insertWithOnConflict( Event.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case Live.ALL:

                newUri = ContentUris.withAppendedId( Live.CONTENT_URI, db.insertWithOnConflict( Live.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case Guest.ALL:

                newUri = ContentUris.withAppendedId( Guest.CONTENT_URI, db.insertWithOnConflict( Guest.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case Episode.ALL:

                newUri = ContentUris.withAppendedId( Episode.CONTENT_URI, db.insertWithOnConflict( Episode.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case Detail.ALL:

                newUri = ContentUris.withAppendedId( Detail.CONTENT_URI, db.insertWithOnConflict( Detail.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case Image.ALL:

                newUri = ContentUris.withAppendedId( Image.CONTENT_URI, db.insertWithOnConflict( Image.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case EpisodeGuests.ALL:

                newUri = ContentUris.withAppendedId( EpisodeGuests.CONTENT_URI, db.insertWithOnConflict( EpisodeGuests.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case WorkItem.ALL:

                newUri = ContentUris.withAppendedId( WorkItem.CONTENT_URI, db.insertWithOnConflict( WorkItem.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs ) {
        Log.v( TAG, "delete : enter" );

        final SQLiteDatabase db = database.getWritableDatabase();

        int deleted = 0;

        switch( URI_MATCHER.match( uri ) ) {

            case Show.ALL:

                deleted = db.delete( Show.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Show.SINGLE:

                deleted = db.delete( Show.TABLE_NAME, Show._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Event.ALL:

                deleted = db.delete( Event.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Event.SINGLE:

                deleted = db.delete( Event.TABLE_NAME, Event._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Live.ALL:

                deleted = db.delete( Live.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Live.SINGLE:

                deleted = db.delete( Live.TABLE_NAME, Live._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Guest.ALL:

                deleted = db.delete( Guest.TABLE_NAME, selection, selectionArgs );

                //TODO: Need to delete all from Episode Guests table

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Guest.SINGLE:

                deleted = db.delete( Guest.TABLE_NAME, Guest._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                //TODO: Need to delete select entries from Episode Guests table

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Episode.ALL:

                //TODO: Need to delete all images on the filesystem

                deleted = db.delete( Episode.TABLE_NAME, selection, selectionArgs );

                //TODO: Need to delete all from Details table

                //TODO: Need to delete all from Images table

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Episode.SINGLE:

                //TODO: Need to delete select images on the filesystem

                deleted = db.delete( Episode.TABLE_NAME, Live._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                //TODO: Need to delete select details from Details table

                //TODO: Need to delete select images from Images table

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Detail.ALL:

                deleted = db.delete( Detail.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Detail.SINGLE:

                deleted = db.delete( Detail.TABLE_NAME, Detail._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Image.ALL:

                deleted = db.delete( Image.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case Image.SINGLE:

                deleted = db.delete( Image.TABLE_NAME, Image._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case EpisodeGuests.ALL:

                deleted = db.delete( EpisodeGuests.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case EpisodeGuests.SINGLE:

                deleted = db.delete( EpisodeGuests.TABLE_NAME, Live._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case WorkItem.ALL:

                deleted = db.delete( WorkItem.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case WorkItem.SINGLE:

                deleted = db.delete( WorkItem.TABLE_NAME, Image._ID
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
        Log.v( TAG, "update : enter" );

        final SQLiteDatabase db = database.getWritableDatabase();

        int affected = 0;

        switch( URI_MATCHER.match( uri ) ) {

            case Show.ALL:
                return db.update( Show.TABLE_NAME, values, selection, selectionArgs );

            case Show.SINGLE:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                return db.update( Show.TABLE_NAME, values, selection, selectionArgs );

            case Event.ALL:
                return db.update( Event.TABLE_NAME, values, selection, selectionArgs );

            case Event.SINGLE:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                return db.update( Event.TABLE_NAME, values, selection, selectionArgs );

            case Live.ALL:
                return db.update( Live.TABLE_NAME, values, selection, selectionArgs );

            case Live.SINGLE:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                return db.update( Live.TABLE_NAME, values, selection, selectionArgs );

            case Guest.ALL:
                return db.update( Guest.TABLE_NAME, values, selection, selectionArgs );

            case Guest.SINGLE:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                return db.update( Guest.TABLE_NAME, values, selection, selectionArgs );

            case Episode.ALL:
                return db.update( Episode.TABLE_NAME, values, selection, selectionArgs );

            case Episode.SINGLE:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                return db.update( Episode.TABLE_NAME, values, selection, selectionArgs );

            case Detail.ALL:
                return db.update( Detail.TABLE_NAME, values, selection, selectionArgs );

            case Detail.SINGLE:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                return db.update( Detail.TABLE_NAME, values, selection, selectionArgs );

            case Image.ALL:
                return db.update( Image.TABLE_NAME, values, selection, selectionArgs );

            case Image.SINGLE:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                return db.update( Image.TABLE_NAME, values, selection, selectionArgs );

            case EpisodeGuests.ALL:
                return db.update( EpisodeGuests.TABLE_NAME, values, selection, selectionArgs );

            case EpisodeGuests.SINGLE:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                return db.update( EpisodeGuests.TABLE_NAME, values, selection, selectionArgs );

            case WorkItem.ALL:
                return db.update( WorkItem.TABLE_NAME, values, selection, selectionArgs );

            case WorkItem.SINGLE:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                return db.update( WorkItem.TABLE_NAME, values, selection, selectionArgs );

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public ContentProviderResult[] applyBatch( ArrayList<ContentProviderOperation> operations )	 throws OperationApplicationException {
        Log.v( TAG, "applyBatch : enter" );

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
