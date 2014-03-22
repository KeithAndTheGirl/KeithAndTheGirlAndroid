package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 3/18/14.
 */
public class Detail extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "detail";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.details";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.details";
    public static final int ALL 					= 610;
    public static final int SINGLE 				    = 611;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW, DELETE_ROW_BY_SHOWID;

    public static final String FIELD_NOTES = "notes";
    public static final String FIELD_NOTES_DATA_TYPE = "TEXT";

    public static final String FIELD_FORUMURL = "forumurl";
    public static final String FIELD_FORUMURL_DATA_TYPE = "TEXT";

    public static final String FIELD_SHOWID = "showid";
    public static final String FIELD_SHOWID_DATA_TYPE = "INTEGER";

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_NOTES, FIELD_FORUMURL, FIELD_SHOWID,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY_AUTOINCREMENT ).append( ", " );
        createTable.append( FIELD_NOTES ).append( " " ).append( FIELD_NOTES_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_FORUMURL ).append( " " ).append( FIELD_FORUMURL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_SHOWID ).append( " " ).append( FIELD_SHOWID_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( FIELD_NOTES ).append( "," );
        insert.append( FIELD_FORUMURL ).append( "," );
        insert.append( FIELD_SHOWID ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_NOTES ).append( " = ?, " );
        update.append( FIELD_FORUMURL ).append( " = ?, " );
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
