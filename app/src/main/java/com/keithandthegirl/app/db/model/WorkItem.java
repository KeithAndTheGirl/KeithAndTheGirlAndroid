package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 3/21/14.
 */
public class WorkItem extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "workitems";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.workitems";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.workitems";
    public static final int ALL      				= 1000;
    public static final int SINGLE         			= 1001;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW;

    public static final String FIELD_NAME = "name";
    public static final String FIELD_NAME_DATA_TYPE = "TEXT";

    public static final String FIELD_FREQUENCY = "frequency";
    public static final String FIELD_FREQUENCY_DATA_TYPE = "TEXT";

    public static final String FIELD_DOWNLOAD = "download";
    public static final String FIELD_DOWNLOAD_DATA_TYPE = "TEXT";

    public static final String FIELD_ENDPOINT = "endpoint";
    public static final String FIELD_ENDPOINT_DATA_TYPE = "TEXT";

    public static final String FIELD_ADDRESS = "address";
    public static final String FIELD_ADDRESS_DATA_TYPE = "TEXT";

    public static final String FIELD_PARAMETERS = "parameters";
    public static final String FIELD_PARAMETERS_DATA_TYPE = "TEXT";

    public static final String FIELD_ETAG = "etag";
    public static final String FIELD_ETAG_DATA_TYPE = "TEXT";

    public static final String FIELD_LAST_RUN = "last_run";
    public static final String FIELD_LAST_RUN_DATA_TYPE = "INTEGER";

    public static final String FIELD_STATUS = "next_run";
    public static final String FIELD_STATUS_DATA_TYPE = "TEXT";

    public static enum Frequency { ONCE, HOURLY, DAILY, WEEKLY, ON_DEMAND };
    public static enum Status { OK, FAILED, NEVER, NOT_MODIFIED };
    public static enum Download { JSON, JSONARRAY, JPG }

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_NAME, FIELD_FREQUENCY, FIELD_DOWNLOAD, FIELD_ENDPOINT, FIELD_ADDRESS, FIELD_PARAMETERS, FIELD_ETAG, FIELD_LAST_RUN, FIELD_STATUS,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY_AUTOINCREMENT ).append( ", " );
        createTable.append( FIELD_NAME ).append( " " ).append( FIELD_NAME_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_FREQUENCY ).append( " " ).append( FIELD_FREQUENCY_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_DOWNLOAD ).append( " " ).append( FIELD_DOWNLOAD_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_ENDPOINT ).append( " " ).append( FIELD_ENDPOINT_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_ADDRESS ).append( " " ).append( FIELD_ADDRESS_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_PARAMETERS ).append( " " ).append( FIELD_PARAMETERS_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_ETAG ).append( " " ).append( FIELD_ETAG_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_RUN ).append( " " ).append( FIELD_LAST_RUN_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_STATUS ).append( " " ).append( FIELD_STATUS_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( FIELD_NAME ).append( "," );
        insert.append( FIELD_FREQUENCY ).append( "," );
        insert.append( FIELD_DOWNLOAD ).append( "," );
        insert.append( FIELD_ENDPOINT ).append( "," );
        insert.append( FIELD_ADDRESS ).append( "," );
        insert.append( FIELD_PARAMETERS ).append( "," );
        insert.append( FIELD_ETAG ).append( "," );
        insert.append( FIELD_LAST_RUN ).append( "," );
        insert.append( FIELD_STATUS ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,?,?,?,?,?,?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_NAME ).append( " = ?, " );
        update.append( FIELD_FREQUENCY ).append( " = ?, " );
        update.append( FIELD_DOWNLOAD ).append( " = ?, " );
        update.append( FIELD_ENDPOINT ).append( " = ?, " );
        update.append( FIELD_ADDRESS ).append( " = ?, " );
        update.append( FIELD_PARAMETERS ).append( " = ?, " );
        update.append( FIELD_ETAG ).append( " = ?, " );
        update.append( FIELD_LAST_RUN ).append( " = ?, " );
        update.append( FIELD_STATUS ).append( " = ?, " );
        update.append( FIELD_LAST_MODIFIED_DATE ).append( " = ? " );
        update.append( "WHERE " ).append( _ID ).append( " = ? " );

        UPDATE_ROW = update.toString();

        StringBuilder delete = new StringBuilder();

        delete.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        delete.append( "WHERE " ).append( _ID ).append( " = ?" );

        DELETE_ROW = delete.toString();

    }

}
