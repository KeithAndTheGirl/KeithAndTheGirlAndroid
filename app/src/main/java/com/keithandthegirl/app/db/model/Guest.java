package com.keithandthegirl.app.db.model;

import android.net.Uri;

import com.keithandthegirl.app.db.KatgProvider;

/**
 * Created by dmfrey on 3/18/14.
 */
public class Guest extends AbstractBaseDatabase {

    public static final String TABLE_NAME = "guest";

    public static final Uri CONTENT_URI = Uri.parse( "content://" + KatgProvider.AUTHORITY + "/" + TABLE_NAME );
    public static final String CONTENT_TYPE = "vnd.keithandthegirl.cursor.dir/com.keithandthegirl.guests";
    public static final String CONTENT_ITEM_TYPE = "vnd.keithandthegirl.cursor.item/com.keithandthegirl.guests";
    public static final int ALL   					= 500;
    public static final int SINGLE   				= 501;

    public static final String CREATE_TABLE, DROP_TABLE;
    public static final String INSERT_ROW, UPDATE_ROW, DELETE_ROW;

    public static final String FIELD_REALNAME = "realname";
    public static final String FIELD_REALNAME_DATA_TYPE = "TEXT";

    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_DESCRIPTION_DATA_TYPE = "TEXT";

    public static final String FIELD_PICTUREFILENAME = "picturefilename";
    public static final String FIELD_PICTUREFILENAME_DATA_TYPE = "TEXT";

    public static final String FIELD_URL1 = "url1";
    public static final String FIELD_URL1_DATA_TYPE = "TEXT";

    public static final String FIELD_URL2 = "url2";
    public static final String FIELD_URL2_DATA_TYPE = "TEXT";

    public static final String FIELD_PICTUREURL = "pictureurl";
    public static final String FIELD_PICTUREURL_DATA_TYPE = "TEXT";

    public static final String FIELD_PICTUREURLLARGE = "pictureurllarge";
    public static final String FIELD_PICTUREURLLARGE_DATA_TYPE = "TEXT";

    public static final String[] COLUMN_MAP = { _ID,
            FIELD_REALNAME, FIELD_DESCRIPTION, FIELD_PICTUREFILENAME, FIELD_URL1, FIELD_URL2, FIELD_PICTUREURL, FIELD_PICTUREURLLARGE,
            FIELD_LAST_MODIFIED_DATE
    };

    static {

        StringBuilder createTable = new StringBuilder();

        createTable.append( "CREATE TABLE " + TABLE_NAME + " (" );
        createTable.append( _ID ).append( " " ).append( FIELD_ID_DATA_TYPE ).append( " " ).append( FIELD_ID_PRIMARY_KEY ).append( ", " );
        createTable.append( FIELD_REALNAME ).append( " " ).append( FIELD_REALNAME_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_DESCRIPTION ).append( " " ).append( FIELD_DESCRIPTION_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_PICTUREFILENAME ).append( " " ).append( FIELD_PICTUREFILENAME_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_URL1 ).append( " " ).append( FIELD_URL1_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_URL2 ).append( " " ).append( FIELD_URL2_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_PICTUREURL ).append( " " ).append( FIELD_PICTUREURL_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_PICTUREURLLARGE ).append( " " ).append( FIELD_PICTUREURLLARGE_DATA_TYPE ).append( ", " );
        createTable.append( FIELD_LAST_MODIFIED_DATE ).append( " " ).append( FIELD_LAST_MODIFIED_DATE_DATA_TYPE );
        createTable.append( ");" );

        CREATE_TABLE = createTable.toString();

        StringBuilder dropTable = new StringBuilder();

        dropTable.append( "DROP TABLE IF EXISTS " ).append( TABLE_NAME );

        DROP_TABLE = dropTable.toString();

        StringBuilder insert = new StringBuilder();

        insert.append( "INSERT INTO " ).append( TABLE_NAME ).append( " ( " );
        insert.append( _ID ).append( "," );
        insert.append( FIELD_REALNAME ).append( "," );
        insert.append( FIELD_DESCRIPTION ).append( "," );
        insert.append( FIELD_PICTUREFILENAME ).append( "," );
        insert.append( FIELD_URL1 ).append( "," );
        insert.append( FIELD_URL2 ).append( "," );
        insert.append( FIELD_PICTUREURL ).append( "," );
        insert.append( FIELD_PICTUREURLLARGE ).append( "," );
        insert.append( FIELD_LAST_MODIFIED_DATE );
        insert.append( " ) " );
        insert.append( "VALUES( ?,?,?,?,?,?,?,?,? )" );

        INSERT_ROW = insert.toString();

        StringBuilder update = new StringBuilder();

        update.append( "UPDATE " ).append( TABLE_NAME ).append( " SET " );
        update.append( FIELD_REALNAME ).append( " = ?, " );
        update.append( FIELD_DESCRIPTION ).append( " = ?, " );
        update.append( FIELD_PICTUREFILENAME ).append( " = ?, " );
        update.append( FIELD_URL1 ).append( " = ?, " );
        update.append( FIELD_URL2 ).append( " = ?, " );
        update.append( FIELD_PICTUREURL ).append( " = ?, " );
        update.append( FIELD_PICTUREURLLARGE ).append( " = ?, " );
        update.append( FIELD_LAST_MODIFIED_DATE ).append( " = ? " );
        update.append( "WHERE " ).append( _ID ).append( " = ? " );

        UPDATE_ROW = update.toString();

        StringBuilder delete = new StringBuilder();

        delete.append( "DELETE FROM " ).append( TABLE_NAME ).append( " " );
        delete.append( "WHERE " ).append( _ID ).append( " = ?" );

        DELETE_ROW = delete.toString();
    }

}
