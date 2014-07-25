package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 3/18/14.
 */
public class EventConstants extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "event";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.events";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.events";
    public static final int ALL    					= 300;
    public static final int SINGLE     				= 301;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW;

    public static final String FIELD_EVENTID = "eventid";
    public static final String FIELD_EVENTID_DATA_TYPE = "TEXT";

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_TITLE_DATA_TYPE = "TEXT";

    public static final String FIELD_LOCATION = "location";
    public static final String FIELD_LOCATION_DATA_TYPE = "TEXT";

    public static final String FIELD_STARTDATE = "startdate";
    public static final String FIELD_STARTDATE_DATA_TYPE = "INTEGER";

    public static final String FIELD_ENDDATE = "enddate";
    public static final String FIELD_ENDDATE_DATA_TYPE = "integer";

    public static final String FIELD_DETAILS = "details";
    public static final String FIELD_DETAILS_DATA_TYPE = "TEXT";

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_TITLE, FIELD_LOCATION, FIELD_STARTDATE, FIELD_ENDDATE, FIELD_DETAILS,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY_AUTOINCREMENT ).append( ", " );
        createTable.append( FIELD_EVENTID ).append( " " ).append( FIELD_EVENTID_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_TITLE ).append( " " ).append( FIELD_TITLE_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LOCATION ).append( " " ).append( FIELD_LOCATION_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_STARTDATE ).append( " " ).append( FIELD_STARTDATE_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_ENDDATE ).append( " " ).append( FIELD_ENDDATE_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_DETAILS ).append( " " ).append( FIELD_DETAILS_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( FIELD_EVENTID ).append( "," );
        insert.append( FIELD_TITLE ).append( "," );
        insert.append( FIELD_LOCATION ).append( "," );
        insert.append( FIELD_STARTDATE ).append( "," );
        insert.append( FIELD_ENDDATE ).append( "," );
        insert.append( FIELD_DETAILS ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,?,?,?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_EVENTID ).append( " = ?, " );
        update.append( FIELD_TITLE ).append( " = ?, " );
        update.append( FIELD_LOCATION ).append( " = ?, " );
        update.append( FIELD_STARTDATE ).append( " = ?, " );
        update.append( FIELD_ENDDATE ).append( " = ?, " );
        update.append( FIELD_DETAILS ).append( " = ?, " );
        update.append( FIELD_LAST_MODIFIED_DATE ).append( " = ? " );
        update.append( "WHERE " ).append( _ID ).append( " = ? " );

        UPDATE_ROW = update.toString();

        StringBuilder delete = new StringBuilder();

        delete.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        delete.append( "WHERE " ).append( _ID ).append( " = ?" );

        DELETE_ROW = delete.toString();
    }

}
