package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 3/19/14.
 */
public class EpisodeGuestConstants extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "episode_guests";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.episodeGuests";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.episodeGuests";
    public static final int ALL 					= 630;
    public static final int SINGLE     				= 631;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW;

    public static final String FIELD_SHOWID = "showid";
    public static final String FIELD_SHOWID_DATA_TYPE = "INTEGER";

    public static final String FIELD_SHOWGUESTID = "showguestid";
    public static final String FIELD_SHOWGUESTID_DATA_TYPE = "INTEGER";

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_SHOWID, FIELD_SHOWGUESTID,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY_AUTOINCREMENT ).append( ", " );
        createTable.append( FIELD_SHOWID ).append( " " ).append( FIELD_SHOWID_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_SHOWGUESTID ).append( " " ).append( FIELD_SHOWGUESTID_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE ).append( ", " );
        createTable.append( "UNIQUE (" ).append( FIELD_SHOWID ).append( ", " ).append( FIELD_SHOWGUESTID ).append( ")" );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( FIELD_SHOWID ).append( "," );
        insert.append( FIELD_SHOWGUESTID ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_SHOWID ).append( "= ?," );
        update.append( FIELD_SHOWGUESTID ).append( "= ?," );
        update.append( FIELD_LAST_MODIFIED_DATE ).append( "= ? " );
        update.append( "WHERE " );
        update.append( _ID ).append( " = ?" );

        UPDATE_ROW = update.toString();

        StringBuilder delete = new StringBuilder();

        delete.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        delete.append( "WHERE " ).append( FIELD_SHOWID ).append( " = ? AND " ).append( FIELD_SHOWGUESTID ).append( " = ?" );

        DELETE_ROW = delete.toString();

    }

}
