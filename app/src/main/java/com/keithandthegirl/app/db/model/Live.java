package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 3/18/14.
 */
public class Live extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "live";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.lives";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.lives";
    public static final int ALL    					= 400;
    public static final int SINGLE				    = 401;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW;

    public static final String FIELD_BROADCASTING = "broadcasting";
    public static final String FIELD_BROADCASTING_DATA_TYPE = "INTEGER";
    public static final String FIELD_BROADCASTING_DEFAULT = "0";

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_BROADCASTING,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY ).append( ", " );
        createTable.append( FIELD_BROADCASTING ).append( " " ).append( FIELD_BROADCASTING_DATA_TYPE ).append( " DEFAULT " ).append( FIELD_BROADCASTING_DEFAULT ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( _ID ).append( ", " );
        insert.append( FIELD_BROADCASTING ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_BROADCASTING ).append( " = ?, " );
        update.append( FIELD_LAST_MODIFIED_DATE ).append( " = ? " );
        update.append( "WHERE " ).append( _ID ).append( " = ? " );

        UPDATE_ROW = update.toString();

        StringBuilder delete = new StringBuilder();

        delete.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        delete.append( "WHERE " ).append( _ID ).append( " = ?" );

        DELETE_ROW = delete.toString();
    }

}
