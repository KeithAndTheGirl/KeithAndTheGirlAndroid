package com.keithandthegirl.app.services.download;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.DetailConstants;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.ui.episodesimpler.EpisodeActivity;
import com.keithandthegirl.app.ui.episodesimpler.EpisodeInfoHolder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by dmfrey on 8/28/14.
 */
public class DownloadBroadcastReciever extends BroadcastReceiver {

    private static final String TAG = DownloadBroadcastReciever.class.getSimpleName();

    public static int notificatinId = 1;

    @Override
    public void onReceive( Context context, Intent intent ) {
        Log.i( TAG, "onRecieve : action=" + intent.getAction() );

        if( DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals( intent.getAction() ) ) {
            Log.i( TAG, "onRecieve : downloadId=" + intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) );
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if(downloadId == 0) {
                return;
            }

            long episodeId = loadEpisodeId( context, downloadId );
            if( episodeId != -1 ) {

                DownloadManager mgr = (DownloadManager) context.getSystemService( Context.DOWNLOAD_SERVICE );
                Uri uri = mgr.getUriForDownloadedFile( downloadId );
                Log.i( TAG, "onRecieve : download uri=" + uri.getEncodedPath() );

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cur = mgr.query(query);
                if (cur.moveToFirst()) {
                    int columnIndex = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == cur.getInt(columnIndex)) {
                        String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                        OutputStream os = null;
                        try {

                            String tmpFilename = Uri.parse(uriString).getLastPathSegment();
                            String path = Uri.parse(uriString).getPath();
                            File inFile = new File(path);
                            File tmpFile = File.createTempFile( tmpFilename, null );
                            tmpFile.deleteOnExit();
                            FileUtils.copyFile( inFile, tmpFile );

                            ContentValues values = new ContentValues();
                            values.put( EpisodeConstants.FIELD_DOWNLOAD_ID, -1 );
                            values.put( EpisodeConstants.FIELD_DOWNLOADED, 1 );
                            int updated = context.getContentResolver().update( ContentUris.withAppendedId( EpisodeConstants.CONTENT_URI, episodeId ), values, null, null );
                            Log.i( TAG, "onRecieve : updated=" + updated );

                            mgr.remove(downloadId);

                            File outFile = new File(path);
                            FileUtils.copyFile( tmpFile, outFile );

                            EpisodeInfoHolder episodeHolder = loadEpisode( context, episodeId );
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentTitle("KATG Download Complete!")
                                            .setContentText(episodeHolder.getShowPrefix() + ":" + episodeHolder.getEpisodeNumber() + " - " + episodeHolder.getEpisodeTitle() )
                                            .setAutoCancel( true );

                            Intent episodeIntent = new Intent(context, EpisodeActivity.class);
                            episodeIntent.putExtra(EpisodeActivity.EPISODE_KEY, episodeId);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                            stackBuilder.addParentStack(EpisodeActivity.class);
                            stackBuilder.addNextIntent(episodeIntent);
                            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                            mBuilder.setContentIntent(resultPendingIntent);
                            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify( notificatinId, mBuilder.build() );

                        } catch( IOException e ) {
                            Log.e(TAG, "onReceive : error reading file", e);
                        }

                    }
                }
                cur.close();

            }
        }

        if( DownloadManager.ACTION_NOTIFICATION_CLICKED.equals( intent.getAction() ) ) {
            Log.i( TAG, "onRecieve : ids=" + Arrays.toString( intent.getLongArrayExtra( DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS ) ) );
            long[] ids = intent.getLongArrayExtra( DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS );
            if( ids.length == 1 ) {

                long episodeId = loadEpisodeId( context, ids[ 0 ] );
                if( episodeId != -1 ) {
                    Log.i( TAG, "onRecieve : launching episode : id=" + episodeId );

                    Intent episodeIntent = new Intent( context, EpisodeActivity.class );
                    episodeIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                    episodeIntent.putExtra( EpisodeActivity.EPISODE_KEY, episodeId );
                    context.startActivity( episodeIntent );

                }

            } else {

                Intent episodeQueue = new Intent();
                episodeQueue.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                episodeQueue.setAction( DownloadManager.ACTION_VIEW_DOWNLOADS );
                context.startActivity(episodeQueue);

            }
        }

    }

    private long loadEpisodeId( Context context, long downloadId ) {

        long episodeId = -1;
        Cursor cursor = context.getContentResolver().query( EpisodeConstants.CONTENT_URI, new String[] { EpisodeConstants._ID }, EpisodeConstants.FIELD_DOWNLOAD_ID + " = ?", new String[] { String.valueOf( downloadId ) }, null );
        if( cursor.moveToNext() ) {
            episodeId = cursor.getLong( cursor.getColumnIndex( EpisodeConstants._ID ) );
        }
        cursor.close();

        return episodeId;
    }

    private EpisodeInfoHolder loadEpisode( Context context, long episodeId ) {

        EpisodeInfoHolder episodeHolder = new EpisodeInfoHolder();

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, episodeId), null, null, null, null);
        if (cursor.moveToNext()) {
            episodeHolder.setEpisodeNumber(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_NUMBER)));
            episodeHolder.setEpisodeTitle(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_TITLE)));
            episodeHolder.setEpisodePreviewUrl(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_PREVIEWURL)));
            episodeHolder.setEpisodeFileUrl(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_FILEURL)));
            episodeHolder.setEpisodeFilename(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_FILENAME)));
            episodeHolder.setEpisodeLength(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_LENGTH)));
            episodeHolder.setEpisodeFileSize(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_FILESIZE)));
            episodeHolder.setEpisodeType(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_TYPE)));
            episodeHolder.setEpisodePublic(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_PUBLIC)) == 1);
            episodeHolder.setEpisodePosted(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_POSTED)));
            episodeHolder.setEpisodeDownloadId(cursor.getLong(cursor.getColumnIndex(EpisodeConstants.FIELD_DOWNLOAD_ID)));
            episodeHolder.setEpisodeDownloaded(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_DOWNLOADED)) == 1);
            episodeHolder.setEpisodePlayed(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_PLAYED)));
            episodeHolder.setEpisodeLastPlayed(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_LASTPLAYED)));
            episodeHolder.setShowNameId(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_SHOWNAMEID)));
            episodeHolder.setEpisodeDetailNotes(cursor.getString(cursor.getColumnIndex(DetailConstants.TABLE_NAME + "_" + DetailConstants.FIELD_NOTES)));
            episodeHolder.setEpisodeDetailForumUrl(cursor.getString(cursor.getColumnIndex(DetailConstants.TABLE_NAME + "_" + DetailConstants.FIELD_FORUMURL)));
            episodeHolder.setShowName(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_NAME)));
            episodeHolder.setShowPrefix(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_PREFIX)));
            episodeHolder.setShowVip(cursor.getInt(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_VIP)) == 1 ? true : false);
            episodeHolder.setShowCoverImageUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_COVERIMAGEURL_200)));
            episodeHolder.setShowForumUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_FORUMURL)));
            episodeHolder.setGuestNames(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_GUEST_NAMES)));

            String guestImages = cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_GUEST_IMAGES));
            if(null != guestImages && !"".equals(guestImages)) {
                String[] images = guestImages.split(",");
                episodeHolder.setEpisodeGuestImages(Arrays.asList(images));
            } else {
                episodeHolder.setEpisodeGuestImages(Collections.EMPTY_LIST);
            }

        }
        cursor.close();


        return episodeHolder;
    }

}
