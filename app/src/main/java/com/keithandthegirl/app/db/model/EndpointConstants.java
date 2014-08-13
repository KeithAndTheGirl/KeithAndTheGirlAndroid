package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 3/19/14.
 */
public class EndpointConstants extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "endpoint";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.endpoints";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.endpoints";
    public static final int ALL      				= 100;
    public static final int SINGLE         			= 101;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW;

    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TYPE_DATA_TYPE = "TEXT";

    public static final String FIELD_URL = "url";
    public static final String FIELD_URL_DATA_TYPE = "TEXT";

    public static final String FIELD_FORMAT = "format";
    public static final String FIELD_FORMAT_DATA_TYPE = "TEXT";

    public static final String FIELD_ETAG = "etag";
    public static final String FIELD_ETAG_DATA_TYPE = "TEXT";

    public static enum Type { EVENTS, LIVE, DETAILS, RECENT, OVERVIEW, LIST, IMAGE, YOUTUBE };
    public static enum DownloadType { ARRAY, OBJECT };

    public static final String EVENTS = "https://www.keithandthegirl.com/api/v2/events/";
    public static final String LIVE = "https://www.keithandthegirl.com/api/v2/feed/live/";
    public static final String DETAILS = "https://www.keithandthegirl.com/api/v2/shows/details/";
    public static final String RECENT = "https://www.keithandthegirl.com/api/v2/shows/recent/";
    public static final String OVERVIEW = "https://www.keithandthegirl.com/api/v2/shows/series-overview/";
    public static final String LIST = "https://www.keithandthegirl.com/api/v2/shows/list/";
    public static final String YOUTUBE = "http://gdata.youtube.com/feeds/base/users/keithandthegirl/uploads?alt=json&v=2&orderby=published&client=ytapi-youtube-profile";

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_TYPE, FIELD_URL, FIELD_FORMAT, FIELD_ETAG,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY_AUTOINCREMENT ).append( ", " );
        createTable.append( FIELD_TYPE ).append( " " ).append( FIELD_TYPE_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_URL ).append( " " ).append( FIELD_URL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_FORMAT ).append( " " ).append( FIELD_FORMAT_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_ETAG ).append( " " ).append( FIELD_ETAG_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( FIELD_TYPE ).append( "," );
        insert.append( FIELD_URL ).append( "," );
        insert.append( FIELD_FORMAT ).append( "," );
        insert.append( FIELD_ETAG ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_TYPE ).append( " = ?, " );
        update.append( FIELD_URL ).append( " = ?, " );
        update.append( FIELD_FORMAT ).append( " = ?, " );
        update.append( FIELD_ETAG ).append( " = ?, " );
        update.append( FIELD_LAST_MODIFIED_DATE ).append( " = ? " );
        update.append( "WHERE " ).append( _ID ).append( " = ? " );

        UPDATE_ROW = update.toString();

        StringBuilder delete = new StringBuilder();

        delete.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        delete.append( "WHERE " ).append( _ID ).append( " = ?" );

        DELETE_ROW = delete.toString();

    }

}
