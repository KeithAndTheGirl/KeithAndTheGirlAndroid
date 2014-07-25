package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 5/21/14.
 */
public class YoutubeConstants extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "youtube";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.youtube_videos";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.youtube_videos";
    public static final int ALL 					= 700;
    public static final int SINGLE     				= 701;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW;


    public static final String FIELD_YOUTUBE_ID = "youtube_id";
    public static final String FIELD_YOUTUBE_ID_DATA_TYPE = "TEXT";

    public static final String FIELD_YOUTUBE_ETAG = "youtube_etag";
    public static final String FIELD_YOUTUBE_ETAG_DATA_TYPE = "TEXT";

    public static final String FIELD_YOUTUBE_TITLE = "youtube_title";
    public static final String FIELD_YOUTUBE_TITLE_DATA_TYPE = "TEXT";

    public static final String FIELD_YOUTUBE_LINK = "youtube_link";
    public static final String FIELD_YOUTUBE_LINK_DATA_TYPE = "TEXT";

    public static final String FIELD_YOUTUBE_THUMBNAIL = "youtube_thumbnail";
    public static final String FIELD_YOUTUBE_THUMBNAIL_DATA_TYPE = "TEXT";

    public static final String FIELD_YOUTUBE_PUBLISHED = "youtube_published";
    public static final String FIELD_YOUTUBE_PUBLISHED_DATA_TYPE = "INTEGER";

    public static final String FIELD_YOUTUBE_UPDATED = "youtube_updated";
    public static final String FIELD_YOUTUBE_UPDATED_DATA_TYPE = "INTEGER";

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_YOUTUBE_ID, FIELD_YOUTUBE_ETAG, FIELD_YOUTUBE_TITLE, FIELD_YOUTUBE_LINK, FIELD_YOUTUBE_THUMBNAIL, FIELD_YOUTUBE_PUBLISHED, FIELD_YOUTUBE_UPDATED,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY_AUTOINCREMENT ).append( ", " );
        createTable.append( FIELD_YOUTUBE_ID ).append( " " ).append( FIELD_YOUTUBE_ID_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_YOUTUBE_ETAG ).append( " " ).append( FIELD_YOUTUBE_ETAG_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_YOUTUBE_TITLE ).append( " " ).append( FIELD_YOUTUBE_TITLE_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_YOUTUBE_LINK ).append( " " ).append( FIELD_YOUTUBE_LINK_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_YOUTUBE_THUMBNAIL ).append( " " ).append( FIELD_YOUTUBE_THUMBNAIL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_YOUTUBE_PUBLISHED ).append( " " ).append( FIELD_YOUTUBE_PUBLISHED_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_YOUTUBE_UPDATED ).append( " " ).append( FIELD_YOUTUBE_UPDATED_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( FIELD_YOUTUBE_ID ).append( "," );
        insert.append( FIELD_YOUTUBE_ETAG ).append( "," );
        insert.append( FIELD_YOUTUBE_TITLE ).append( "," );
        insert.append( FIELD_YOUTUBE_LINK ).append( "," );
        insert.append( FIELD_YOUTUBE_THUMBNAIL ).append( "," );
        insert.append( FIELD_YOUTUBE_PUBLISHED ).append( "," );
        insert.append( FIELD_YOUTUBE_UPDATED ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,?,?,?,?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_YOUTUBE_ID ).append( " = ?, " );
        update.append( FIELD_YOUTUBE_ETAG ).append( " = ?, " );
        update.append( FIELD_YOUTUBE_TITLE ).append( " = ?, " );
        update.append( FIELD_YOUTUBE_LINK ).append( " = ?, " );
        update.append( FIELD_YOUTUBE_THUMBNAIL ).append( " = ?, " );
        update.append( FIELD_YOUTUBE_PUBLISHED ).append( " = ?, " );
        update.append( FIELD_YOUTUBE_UPDATED ).append( " = ?, " );
        update.append( FIELD_LAST_MODIFIED_DATE ).append( " = ? " );
        update.append( "WHERE " ).append( _ID ).append( " = ? " );

        UPDATE_ROW = update.toString();

        StringBuilder delete = new StringBuilder();

        delete.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        delete.append( "WHERE " ).append( _ID ).append( " = ?" );

        DELETE_ROW = delete.toString();
    }

}
