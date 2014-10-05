package com.keithandthegirl.app.services.media;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.ShowConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Retrieves and organizes media to play. Before being used, you must call {@link #prepare()},
 * which will retrieve all of the music on the user's device (by performing a query on a content
 * resolver). After that, it's ready to retrieve a random song, with its title and URI, upon
 * request.
 */
public class EpisodeRetriever {
    final String TAG = EpisodeRetriever.class.getSimpleName();

    ContentResolver mContentResolver;
    Uri mEpisodeUri;

    Item mItem;

    public EpisodeRetriever(ContentResolver cr, Uri episodeUri) {

        mContentResolver = cr;
        mEpisodeUri = episodeUri;

    }

    /**
     * Loads music data. This method may take long, so be sure to call it asynchronously without
     * blocking the main thread.
     */
    public void prepare() {

        // Perform a query on the content resolver. The URI we're passing specifies that we
        // want to query for all audio media on external storage (e.g. SD card)
        Cursor cur = mContentResolver.query(mEpisodeUri, null, null, null, null);
        Log.i(TAG, "Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));

        if (cur == null) {
            // Query failed...
            Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return;
        }

        if (!cur.moveToFirst()) {
            // Nothing to query. There is no music on the device. How boring.
            Log.e(TAG, "Failed to move cursor to first row (no query results).");
            return;
        }
        Log.i(TAG, "Listing...");

        // retrieve the indices of the columns where the ID, title, etc. of the song are
        int episodeIdColumn = cur.getColumnIndex(EpisodeConstants._ID);
        int episodeNumberColumn = cur.getColumnIndex(EpisodeConstants.FIELD_NUMBER);
        int episodeTitleColumn = cur.getColumnIndex(EpisodeConstants.FIELD_TITLE);
        int showNameColumn = cur.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_NAME);
        int showPrefixColumn = cur.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_PREFIX);
        int showCoverImageUrlColumn = cur.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_COVERIMAGEURL_200);
        int episodeUrlColumn = cur.getColumnIndex(EpisodeConstants.FIELD_FILEURL);
        int episodeFilenameColumn = cur.getColumnIndex(EpisodeConstants.FIELD_FILENAME);
        int episodeDownloadedColumn = cur.getColumnIndex(EpisodeConstants.FIELD_DOWNLOADED);
        int episodeDurationColumn = cur.getColumnIndex(EpisodeConstants.FIELD_LENGTH);
        int episodeLastPlayedColumn = cur.getColumnIndex(EpisodeConstants.FIELD_LASTPLAYED);

        mItem = new Item(
                cur.getLong(episodeIdColumn),
                cur.getInt(episodeNumberColumn),
                cur.getString(episodeTitleColumn),
                cur.getString(showNameColumn),
                cur.getString(showPrefixColumn),
                cur.getString(showCoverImageUrlColumn),
                cur.getString(episodeUrlColumn),
                cur.getString(episodeFilenameColumn),
                (cur.getInt(episodeDownloadedColumn) == 1 ? true : false),
                cur.getLong(episodeDurationColumn),
                cur.getInt(episodeLastPlayedColumn));

        cur.close();

        Log.i(TAG, "Done querying media. EpisodeRetriever is ready.");
    }

    public void updateLastPlayed( int currentPosition ) {

        if( mItem == null ) {
            return;
        }

        mItem.lastPlayed = currentPosition;

        ContentValues values = new ContentValues();
        values.put(EpisodeConstants.FIELD_PLAYED, 1 );
        values.put(EpisodeConstants.FIELD_LASTPLAYED, currentPosition );

        getContentResolver().update(ContentUris.withAppendedId( EpisodeConstants.CONTENT_URI, mItem.id ), values, null, null );

    }

    public ContentResolver getContentResolver() {
        return mContentResolver;
    }

    public Item getItem() { return mItem; }

    public static class Item {
        long id;
        int number;
        String title;
        String name;
        String prefix;
        String coverImageUrl;
        String episodeUrl;
        String episodeFilename;
        boolean downloaded;
        long duration;
        int lastPlayed;

        public Item(long id, int number, String title, String name, String prefix, String coverImageUrl, String episodeUrl, String episodeFilename, boolean downloaded, long duration, int lastPlayed) {
            this.id = id;
            this.number = number;
            this.title = title;
            this.name = name;
            this.prefix = prefix;
            this.coverImageUrl = coverImageUrl;
            this.episodeUrl = episodeUrl;
            this.episodeFilename = episodeFilename;
            this.downloaded = downloaded;
            this.duration = duration;
            this.lastPlayed = lastPlayed;
        }

        public Item( String episodeUrl ) {
            this.episodeUrl = episodeUrl;
        }

        public long getId() {
            return id;
        }

        public int getNumber() {
            return number;
        }

        public String getTitle() {
            return title;
        }

        public String getName() { return name; }

        public String getPrefix() {
            return prefix;
        }

        public String getCoverImageUrl() { return coverImageUrl; }

        public String getEpisodeUrl() { return episodeUrl; }

        public String getEpisodeFilename() { return episodeFilename; }

        public boolean isDownloaded() { return downloaded; }

        public long getDuration() {
            return duration;
        }

        public int getLastPlayed() { return lastPlayed; }

        public Uri getURI() {
            return ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, id);
        }

        public String toString() {
            return prefix + " : " + number;
        }

    }

}
