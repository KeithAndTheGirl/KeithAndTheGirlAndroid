package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 3/18/14.
 */
public class ImageConstants extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "image";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.images";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.images";
    public static final int ALL   					= 620;
    public static final int SINGLE   				= 621;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW, DELETE_ROW_BY_SHOWID;

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_TITLE_DATA_TYPE = "text";

    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_DESCRIPTION_DATA_TYPE = "TEXT";

    public static final String FIELD_EXPLICIT = "explicit";
    public static final String FIELD_EXPLICIT_DATA_TYPE = "INTEGER";
    public static final String FIELD_EXPLICIT_DEFAULT = "0";

    public static final String FIELD_DISPLAY_ORDER = "displayorder";
    public static final String FIELD_DISPLAY_ORDER_DATA_TYPE = "INTEGER";

    public static final String FIELD_MEDIAURL = "mediaurl";
    public static final String FIELD_MEDIAURL_DATA_TYPE = "text";

    public static final String FIELD_SHOWID = "showid";
    public static final String FIELD_SHOWID_DATA_TYPE = "INTEGER";

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_TITLE, FIELD_DESCRIPTION, FIELD_EXPLICIT, FIELD_DISPLAY_ORDER, FIELD_MEDIAURL, FIELD_SHOWID,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY ).append( ", " );
        createTable.append( FIELD_TITLE ).append( " " ).append( FIELD_TITLE_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_DESCRIPTION ).append( " " ).append( FIELD_DESCRIPTION_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_EXPLICIT ).append( " " ).append( FIELD_EXPLICIT_DATA_TYPE ).append( " DEFAULT " ).append( FIELD_EXPLICIT_DEFAULT ).append( ", " );
        createTable.append( FIELD_DISPLAY_ORDER ).append( " " ).append( FIELD_DISPLAY_ORDER_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_MEDIAURL ).append( " " ).append( FIELD_MEDIAURL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_SHOWID ).append( " " ).append( FIELD_SHOWID_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( FIELD_TITLE ).append( "," );
        insert.append( FIELD_DESCRIPTION ).append( "," );
        insert.append( FIELD_EXPLICIT ).append( "," );
        insert.append( FIELD_DISPLAY_ORDER ).append( "," );
        insert.append( FIELD_MEDIAURL ).append( "," );
        insert.append( FIELD_SHOWID ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,?,?,?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_TITLE ).append( " = ?, " );
        update.append( FIELD_DESCRIPTION ).append( " = ?, " );
        update.append( FIELD_EXPLICIT ).append( " = ?, " );
        update.append( FIELD_DISPLAY_ORDER ).append( " = ?, " );
        update.append( FIELD_MEDIAURL ).append( " = ?, " );
        update.append( FIELD_SHOWID ).append( " = ?, " );
        update.append( FIELD_LAST_MODIFIED_DATE ).append( " = ? " );
        update.append( "WHERE " ).append( _ID ).append( " = ? " );

        UPDATE_ROW = update.toString();

        StringBuilder delete = new StringBuilder();

        delete.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        delete.append( "WHERE " ).append( _ID ).append( " = ?" );

        DELETE_ROW = delete.toString();

        StringBuilder deleteShowId = new StringBuilder();

        deleteShowId.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        deleteShowId.append( "WHERE " ).append( FIELD_SHOWID ).append( " = ?" );

        DELETE_ROW_BY_SHOWID = deleteShowId.toString();

    }

}
