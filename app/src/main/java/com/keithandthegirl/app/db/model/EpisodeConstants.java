package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 3/18/14.
 */
public class EpisodeConstants extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "episode";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.episodes";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.episodes";
    public static final int ALL 					= 600;
    public static final int SINGLE     				= 601;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW;

    public static final String FIELD_NUMBER = "number";
    public static final String FIELD_NUMBER_DATA_TYPE = "INTEGER";

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_TITLE_DATA_TYPE = "TEXT";

    public static final String FIELD_VIDEOFILEURL = "videofileurl";
    public static final String FIELD_VIDEOFILEURL_DATA_TYPE = "TEXT";

    public static final String FIELD_VIDEOTHUMBNAILURL = "videothumbnailurl";
    public static final String FIELD_VIDEOTHUMBNAILURL_DATA_TYPE = "TEXT";

    public static final String FIELD_PREVIEWURL = "previewurl";
    public static final String FIELD_PREVIEWURL_DATA_TYPE = "TEXT";

    public static final String FIELD_FILEURL = "fileurl";
    public static final String FIELD_FILEURL_DATA_TYPE = "TEXT";

    public static final String FIELD_FILENAME = "filename";
    public static final String FIELD_FILENAME_DATA_TYPE = "TEXT";

    public static final String FIELD_LENGTH = "length";
    public static final String FIELD_LENGTH_DATA_TYPE = "INTEGER";

    public static final String FIELD_FILESIZE = "filesize";
    public static final String FIELD_FILESIZE_DATA_TYPE = "INTEGER";

    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TYPE_DATA_TYPE = "INTEGER";

    public static final String FIELD_PUBLIC = "public";
    public static final String FIELD_PUBLIC_DATA_TYPE = "INTEGER";

    public static final String FIELD_POSTED = "posted";
    public static final String FIELD_POSTED_DATA_TYPE = "STRING";

    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_TIMESTAMP_DATA_TYPE = "INTEGER";

    public static final String FIELD_DOWNLOADED = "downloaded";
    public static final String FIELD_DOWNLOADED_DATA_TYPE = "INTEGER";
    public static final String FIELD_DOWNLOADED_DEFAULT = "0";

    public static final String FIELD_PLAYED = "played";
    public static final String FIELD_PLAYED_DATA_TYPE = "INTEGER";

    public static final String FIELD_LASTPLAYED = "lastplayed";
    public static final String FIELD_LASTPLAYED_DATA_TYPE = "INTEGER";

    public static final String FIELD_SHOWNAMEID = "shownameid";
    public static final String FIELD_SHOWNAMEID_DATA_TYPE = "INTEGER";

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_NUMBER, FIELD_TITLE, FIELD_VIDEOFILEURL, FIELD_VIDEOTHUMBNAILURL, FIELD_PREVIEWURL, FIELD_FILEURL, FIELD_FILENAME, FIELD_LENGTH, FIELD_FILESIZE, FIELD_TYPE, FIELD_PUBLIC, FIELD_POSTED, FIELD_TIMESTAMP,
            FIELD_DOWNLOADED, FIELD_PLAYED, FIELD_LASTPLAYED, FIELD_SHOWNAMEID,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY ).append( ", " );
        createTable.append( FIELD_NUMBER ).append( " " ).append( FIELD_NUMBER_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_TITLE ).append( " " ).append( FIELD_TITLE_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_VIDEOFILEURL ).append( " " ).append( FIELD_VIDEOFILEURL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_VIDEOTHUMBNAILURL ).append( " " ).append( FIELD_VIDEOTHUMBNAILURL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_PREVIEWURL ).append( " " ).append( FIELD_PREVIEWURL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_FILEURL ).append( " " ).append( FIELD_FILEURL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_FILENAME ).append( " " ).append( FIELD_FILENAME_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LENGTH ).append( " " ).append( FIELD_LENGTH_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_FILESIZE ).append( " " ).append( FIELD_FILESIZE_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_TYPE ).append( " " ).append( FIELD_TYPE_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_PUBLIC ).append( " " ).append( FIELD_PUBLIC_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_POSTED ).append( " " ).append( FIELD_POSTED_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_TIMESTAMP ).append( " " ).append( FIELD_TIMESTAMP_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_DOWNLOADED ).append( " " ).append( FIELD_DOWNLOADED_DATA_TYPE ).append( " DEFAULT " ).append( FIELD_DOWNLOADED_DEFAULT ).append( ", " );
        createTable.append( FIELD_PLAYED ).append( " " ).append( FIELD_PLAYED_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LASTPLAYED ).append( " " ).append( FIELD_LASTPLAYED_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_SHOWNAMEID ).append( " " ).append( FIELD_SHOWNAMEID_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( _ID ).append( "," );
        insert.append( FIELD_NUMBER ).append( "," );
        insert.append( FIELD_TITLE ).append( "," );
        insert.append( FIELD_VIDEOFILEURL ).append( "," );
        insert.append( FIELD_VIDEOTHUMBNAILURL ).append( "," );
        insert.append( FIELD_PREVIEWURL ).append( "," );
        insert.append( FIELD_FILEURL ).append( "," );
        insert.append( FIELD_FILENAME ).append( "," );
        insert.append( FIELD_LENGTH ).append( "," );
        insert.append( FIELD_FILESIZE ).append( "," );
        insert.append( FIELD_TYPE ).append( "," );
        insert.append( FIELD_PUBLIC ).append( "," );
        insert.append( FIELD_POSTED ).append( "," );
        insert.append( FIELD_TIMESTAMP ).append( "," );
        insert.append( FIELD_DOWNLOADED ).append( "," );
        insert.append( FIELD_PLAYED ).append( "," );
        insert.append( FIELD_LASTPLAYED ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE ).append( "," );
        insert.append( FIELD_SHOWNAMEID );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_NUMBER ).append( " = ?, " );
        update.append( FIELD_TITLE ).append( " = ?, " );
        update.append( FIELD_VIDEOFILEURL ).append( " = ?, " );
        update.append( FIELD_VIDEOTHUMBNAILURL ).append( " = ?, " );
        update.append( FIELD_PREVIEWURL ).append( " = ?, " );
        update.append( FIELD_FILEURL ).append( " = ?, " );
        update.append( FIELD_FILENAME ).append( " = ?, " );
        update.append( FIELD_LENGTH ).append( " = ?, " );
        update.append( FIELD_FILESIZE ).append( " = ?, " );
        update.append( FIELD_TYPE ).append( " = ?, " );
        update.append( FIELD_PUBLIC ).append( " = ?, " );
        update.append( FIELD_POSTED ).append( " = ?, " );
        update.append( FIELD_TIMESTAMP ).append( " = ?, " );
        update.append( FIELD_DOWNLOADED ).append( " = ?, " );
        update.append( FIELD_PLAYED ).append( " = ?, " );
        update.append( FIELD_LASTPLAYED ).append( " = ?, " );
        update.append( FIELD_LAST_MODIFIED_DATE ).append( " = ?, " );
        update.append( FIELD_SHOWNAMEID ).append( " = ? " );
        update.append( "WHERE " ).append( _ID ).append( " = ? " );

        UPDATE_ROW = update.toString();

        StringBuilder delete = new StringBuilder();

        delete.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        delete.append( "WHERE " ).append( _ID ).append( " = ?" );

        DELETE_ROW = delete.toString();
    }

}
