package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 3/19/14.
 */
public class Show extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "show";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.shows";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.shows";
    public static final int ALL		    			= 200;
    public static final int SINGLE    				= 201;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW;

    public static final String FIELD_NAME = "name";
    public static final String FIELD_NAME_DATA_TYPE = "TEXT";

    public static final String FIELD_PREFIX = "prefix";
    public static final String FIELD_PREFIX_DATA_TYPE = "TEXT";

    public static final String FIELD_VIP = "vip";
    public static final String FIELD_VIP_DATA_TYPE = "INTEGER";

    public static final String FIELD_SORTORDER = "sortorder";
    public static final String FIELD_SORTORDER_DATA_TYPE = "INTEGER";

    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_DESCRIPTION_DATA_TYPE = "TEXT";

    public static final String FIELD_COVERIMAGEURL = "coverimageurl";
    public static final String FIELD_COVERIMAGEURL_DATA_TYPE = "TEXT";

    public static final String FIELD_FORUMURL = "FORUMURL";
    public static final String FIELD_FORUMURL_DATA_TYPE = "TEXT";

    public static final String FIELD_PREVIEWURL = "previewurl";
    public static final String FIELD_PREVIEWURL_DATA_TYPE = "TEXT";

    public static final String FIELD_EPISODE_COUNT = "episode_count";
    public static final String FIELD_EPISODE_COUNT_DATA_TYPE = "INTEGER";

    public static final String FIELD_EPISODE_COUNT_MAX = "episode_count_max";
    public static final String FIELD_EPISODE_COUNT_MAX_DATA_TYPE = "INTEGER";

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_NAME, FIELD_PREFIX, FIELD_VIP, FIELD_SORTORDER, FIELD_DESCRIPTION, FIELD_COVERIMAGEURL, FIELD_FORUMURL, FIELD_PREVIEWURL, FIELD_EPISODE_COUNT, FIELD_EPISODE_COUNT_MAX,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY ).append( ", " );
        createTable.append( FIELD_NAME ).append( " " ).append( FIELD_NAME_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_PREFIX).append( " " ).append( FIELD_PREFIX_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_VIP ).append( " " ).append( FIELD_VIP_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_SORTORDER ).append( " " ).append( FIELD_SORTORDER_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_DESCRIPTION ).append( " " ).append( FIELD_DESCRIPTION_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_COVERIMAGEURL ).append( " " ).append( FIELD_COVERIMAGEURL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_FORUMURL ).append( " " ).append( FIELD_FORUMURL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_PREVIEWURL ).append( " " ).append( FIELD_PREVIEWURL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_EPISODE_COUNT ).append( " " ).append( FIELD_EPISODE_COUNT_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_EPISODE_COUNT_MAX ).append( " " ).append( FIELD_EPISODE_COUNT_MAX_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( _ID ).append( "," );
        insert.append( FIELD_NAME ).append( "," );
        insert.append( FIELD_PREFIX ).append( "," );
        insert.append( FIELD_VIP ).append( "," );
        insert.append( FIELD_SORTORDER ).append( "," );
        insert.append( FIELD_DESCRIPTION ).append( "," );
        insert.append( FIELD_COVERIMAGEURL ).append( "," );
        insert.append( FIELD_FORUMURL ).append( "," );
        insert.append( FIELD_PREFIX ).append( "," );
        insert.append( FIELD_EPISODE_COUNT ).append( "," );
        insert.append( FIELD_EPISODE_COUNT_MAX ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,?,?,?,?,?,?,?,?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_NAME ).append( " = ?, " );
        update.append( FIELD_PREFIX ).append( " = ?, " );
        update.append( FIELD_VIP ).append( " = ?, " );
        update.append( FIELD_SORTORDER ).append( " = ?, " );
        update.append( FIELD_DESCRIPTION ).append( " = ?, " );
        update.append( FIELD_COVERIMAGEURL ).append( " = ?, " );
        update.append( FIELD_FORUMURL ).append( " = ?, " );
        update.append( FIELD_PREFIX ).append( " = ?, " );
        update.append( FIELD_EPISODE_COUNT ).append( " = ?, " );
        update.append( FIELD_EPISODE_COUNT_MAX ).append( " = ?, " );
        update.append( FIELD_LAST_MODIFIED_DATE ).append( " = ? " );
        update.append( "WHERE " ).append( _ID ).append( " = ? " );

        UPDATE_ROW = update.toString();

        StringBuilder delete = new StringBuilder();

        delete.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        delete.append( "WHERE " ).append( _ID ).append( " = ?" );

        DELETE_ROW = delete.toString();
    }

}
