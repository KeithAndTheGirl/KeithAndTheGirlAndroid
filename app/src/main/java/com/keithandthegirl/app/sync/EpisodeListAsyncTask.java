package com.keithandthegirl.app.sync;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.EpisodeGuestConstants;
import com.keithandthegirl.app.db.model.Guest;
import com.keithandthegirl.app.db.model.GuestConstants;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by dmfrey on 10/23/14.
 */
public class EpisodeListAsyncTask extends AsyncTask<Void, Void, List<Episode>> {

    private static final String TAG = EpisodeListAsyncTask.class.getSimpleName();
    private static final String[] projection = new String[] { EpisodeConstants._ID, EpisodeConstants.FIELD_DOWNLOADED, EpisodeConstants.FIELD_PLAYED, EpisodeConstants.FIELD_LASTPLAYED };

    public static final String COMPLETE_ACTION = "com.keithandthegirl.app.sync.episodeList.COMPLETE_ACTION";

    private KatgService mKatgService;

    private Context mContext;
    private final int mShowNameId, mShowId, mNumber, mLimit;
    private final boolean mUpdateNewCount;

    public EpisodeListAsyncTask( final Context context, final int showNameId, final int showId, final int number, final int limit, final boolean updateNewCount ) {

        mContext = context;
        mShowNameId = showNameId;
        mShowId = showId;
        mNumber = number;
        mLimit = limit;
        mUpdateNewCount = updateNewCount;

        initializeClient();
    }

    @Override
    protected List<Episode> doInBackground( Void... params ) {

        try {
            List<Episode> episodes = mKatgService.listEpisodes(mShowNameId, mShowId, mNumber, mLimit);

            Log.v( TAG, "doInBackground : exit" );
            return episodes;
        } catch( RetrofitError e ) {

            Log.v( TAG, "doInBackground : error", e );
            return null;
        }

    }

    @Override
    protected void onPostExecute( List<Episode> episodes ) {

        ContentValues values;
        int newSinceLastRun = 0;

        if( null != episodes && !episodes.isEmpty() ) {

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            for( Episode episode : episodes ) {
                Log.v(TAG, "onPostExecute : episode=" + episode.toString());

                String fileName = "";
                try {

                    Uri fileUrl = Uri.parse(episode.getFileUrl());
                    if (null != fileUrl.getLastPathSegment()) {
                        fileName = fileUrl.getLastPathSegment();
                        //Log.v( TAG, "processEpisodes : fileName=" + fileName );
                    }

                } catch (NullPointerException e) {
                }

                values = new ContentValues();
                values.put(EpisodeConstants._ID, episode.getShowId());
                values.put(EpisodeConstants.FIELD_NUMBER, episode.getNumber());
                values.put(EpisodeConstants.FIELD_TITLE, episode.getTitle());
                values.put(EpisodeConstants.FIELD_VIDEOFILEURL, episode.getVideoFileUrl());
                values.put(EpisodeConstants.FIELD_VIDEOTHUMBNAILURL, episode.getVideoThumbnailUrl());
                values.put(EpisodeConstants.FIELD_PREVIEWURL, episode.getPreviewUrl());
                values.put(EpisodeConstants.FIELD_FILEURL, episode.getFileUrl());
                values.put(EpisodeConstants.FIELD_FILENAME, fileName);
                values.put(EpisodeConstants.FIELD_LENGTH, episode.getLength());
                values.put(EpisodeConstants.FIELD_FILESIZE, episode.getFileSize());
                values.put(EpisodeConstants.FIELD_TYPE, episode.getType());
                values.put(EpisodeConstants.FIELD_PUBLIC, episode.getVip());
                values.put(EpisodeConstants.FIELD_POSTED, episode.getPostedDate());
                values.put(EpisodeConstants.FIELD_TIMESTAMP, episode.getTimestamp());
                values.put(EpisodeConstants.FIELD_SHOWNAMEID, episode.getShowNameId());
                values.put(EpisodeConstants.FIELD_LAST_MODIFIED_DATE, new DateTime(DateTimeZone.UTC).getMillis());

                Cursor cursor = mContext.getContentResolver().query(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, episode.getShowId()), projection, null, null, null);
                if (cursor.moveToFirst()) {
                    Log.v(TAG, "onPostExecute : episode iteration, updating existing entry");

                    long downloaded = cursor.getLong(cursor.getColumnIndex(EpisodeConstants.FIELD_DOWNLOADED));
                    long played = cursor.getLong(cursor.getColumnIndex(EpisodeConstants.FIELD_PLAYED));
                    long lastplayed = cursor.getLong(cursor.getColumnIndex(EpisodeConstants.FIELD_LASTPLAYED));

                    values.put(EpisodeConstants.FIELD_DOWNLOADED, downloaded);
                    values.put(EpisodeConstants.FIELD_PLAYED, played);
                    values.put(EpisodeConstants.FIELD_LASTPLAYED, lastplayed);

                    Long id = cursor.getLong(cursor.getColumnIndexOrThrow(EpisodeConstants._ID));
                    ops.add(
                            ContentProviderOperation
                                    .newUpdate(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, id))
                                    .withValues(values)
                                    .build()
                    );

                } else {
                    Log.v(TAG, "onPostExecute : episode iteration, adding new entry");

                    values.put(EpisodeConstants.FIELD_DOWNLOADED, -1);
                    values.put(EpisodeConstants.FIELD_PLAYED, -1);
                    values.put(EpisodeConstants.FIELD_LASTPLAYED, -1);

                    ops.add(
                            ContentProviderOperation
                                    .newInsert(EpisodeConstants.CONTENT_URI)
                                    .withValues(values)
                                    .withYieldAllowed(true)
                                    .build()
                    );

                    newSinceLastRun++;
                }
                cursor.close();

                Log.v(TAG, "onPostExecute : processing guests");
                if (null != episode.getGuests() && episode.getGuests().length > 0) {

                    List<String> guestNames = new ArrayList<String>();
                    List<String> guestIds = new ArrayList<String>();
                    List<String> guestImages = new ArrayList<String>();

                    for (Guest guest : episode.getGuests()) {
                        Log.v(TAG, "onPostExecute : guest=" + guest.toString());

                        guestNames.add(guest.getRealName());
                        guestIds.add(String.valueOf(guest.getShowGuestId()));
                        guestImages.add(guest.getPictureUrlLarge());

                        values = new ContentValues();
                        values.put(GuestConstants._ID, guest.getShowGuestId());
                        values.put(GuestConstants.FIELD_REALNAME, guest.getRealName());
                        values.put(GuestConstants.FIELD_DESCRIPTION, guest.getDescription());
                        values.put(GuestConstants.FIELD_PICTUREFILENAME, guest.getPictureFilename());
                        values.put(GuestConstants.FIELD_URL1, guest.getUrl1());
                        values.put(GuestConstants.FIELD_URL2, guest.getUrl2());
                        values.put(GuestConstants.FIELD_PICTUREURL, guest.getPictureUrl());
                        values.put(GuestConstants.FIELD_PICTUREURLLARGE, guest.getPictureUrlLarge());
                        values.put(GuestConstants.FIELD_LAST_MODIFIED_DATE, new DateTime(DateTimeZone.UTC).getMillis());

                        cursor = mContext.getContentResolver().query(ContentUris.withAppendedId(GuestConstants.CONTENT_URI, guest.getShowGuestId()), null, null, null, null);
                        if (cursor.moveToFirst()) {
                            Log.v(TAG, "onPostExecute : guest iteration, updating existing entry");

                            Long id = cursor.getLong(cursor.getColumnIndexOrThrow(GuestConstants._ID));
                            ops.add(
                                    ContentProviderOperation
                                            .newUpdate(ContentUris.withAppendedId(GuestConstants.CONTENT_URI, id))
                                            .withValues(values)
                                            .build()
                            );

                        } else {
                            Log.v(TAG, "onPostExecute : guest iteration, adding new entry");

                            ops.add(
                                    ContentProviderOperation
                                            .newInsert(GuestConstants.CONTENT_URI)
                                            .withValues(values)
                                            .withYieldAllowed(true)
                                            .build()
                            );

                        }
                        cursor.close();

                        values = new ContentValues();
                        values.put(EpisodeGuestConstants.FIELD_SHOWID, episode.getShowId());
                        values.put(EpisodeGuestConstants.FIELD_SHOWGUESTID, guest.getShowGuestId());
                        values.put(GuestConstants.FIELD_LAST_MODIFIED_DATE, new DateTime(DateTimeZone.UTC).getMillis());

                        cursor = mContext.getContentResolver().query(EpisodeGuestConstants.CONTENT_URI, null, EpisodeGuestConstants.FIELD_SHOWID + "=? and " + EpisodeGuestConstants.FIELD_SHOWGUESTID + "=?", new String[]{String.valueOf(episode.getShowId()), String.valueOf(guest.getShowGuestId())}, null);
                        if (cursor.moveToFirst()) {
                            Log.v(TAG, "processEpisodes : episodeGuest iteration, updating existing entry");

                            Long id = cursor.getLong(cursor.getColumnIndexOrThrow(EpisodeGuestConstants._ID));
                            ops.add(
                                    ContentProviderOperation
                                            .newUpdate(ContentUris.withAppendedId(EpisodeGuestConstants.CONTENT_URI, id))
                                            .withValues(values)
                                            .build()
                            );

                        } else {
                            Log.v(TAG, "onPostExecute : episodeGuest iteration, adding new entry");

                            ops.add(
                                    ContentProviderOperation
                                            .newInsert(EpisodeGuestConstants.CONTENT_URI)
                                            .withValues(values)
                                            .withYieldAllowed(true)
                                            .build()
                            );

                        }
                        cursor.close();
                    }

//                    if (!guestNames.isEmpty()) {
//
//                        values = new ContentValues();
//                        values.put(EpisodeConstants.FIELD_GUEST_NAMES, concatList(guestNames, ","));
//                        values.put(EpisodeConstants.FIELD_GUEST_IDS, concatList(guestIds, ","));
//                        values.put(EpisodeConstants.FIELD_GUEST_IMAGES, concatList(guestImages, ","));
//
//                        ops.add(
//                                ContentProviderOperation
//                                        .newUpdate(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, episode.getShowId()))
//                                        .withValues(values)
//                                        .build()
//                        );
//
//                    }
                }

            }

            if (mUpdateNewCount && newSinceLastRun > 0) {

                values = new ContentValues();
                values.put(ShowConstants.FIELD_EPISODE_COUNT_NEW, newSinceLastRun);

                mContext.getContentResolver().update(ContentUris.withAppendedId(ShowConstants.CONTENT_URI, mShowNameId), values, null, null);
                ops.add(
                        ContentProviderOperation
                                .newUpdate(ContentUris.withAppendedId(ShowConstants.CONTENT_URI, mShowNameId))
                                .withValues(values)
                                .build()
                );

            }

            try {

                mContext.getContentResolver().applyBatch( KatgProvider.AUTHORITY, ops );

            } catch( Exception e ) {

                // Display warning
                CharSequence txt = mContext.getString( R.string.processEpisodesFailure );
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText( mContext, txt, duration );
                toast.show();

                // Log exception
                Log.e( TAG, "processShows : error processing episodes", e );
            }

        }

        Intent completeIntent = new Intent();
        completeIntent.setAction( COMPLETE_ACTION );
        mContext.sendBroadcast( completeIntent );

    }

    private void initializeClient() {

        OkHttpClient client = new OkHttpClient();

        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        File cacheDirectory = new File( mContext.getCacheDir().getAbsolutePath(), "HttpCache" );

        Cache cache = new Cache( cacheDirectory, cacheSize );
        client.setCache( cache );

        Gson katgGson = new GsonBuilder()
                .setDateFormat("MM/dd/yyyy HH:mm")
                .create();

        RestAdapter katgRestAdapter = new RestAdapter.Builder()
                .setEndpoint( KatgService.KATG_URL )
                .setClient( new OkClient( client ) )
                .setConverter( new GsonConverter( katgGson ) )
                .build();

        mKatgService = katgRestAdapter.create( KatgService.class );

    }

    private String concatList( List<String> sList, String separator ) {
        Iterator<String> iter = sList.iterator();
        StringBuilder sb = new StringBuilder();

        while( iter.hasNext() ){
            sb.append( iter.next() ).append( iter.hasNext() ? separator : "" );
        }
        return sb.toString();
    }

}
