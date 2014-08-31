package com.keithandthegirl.app.services.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.ui.episodesimpler.EpisodeActivity;

import java.util.Arrays;

/**
 * Created by dmfrey on 8/28/14.
 */
public class DownloadBroadcastReciever extends BroadcastReceiver {

    private static final String TAG = DownloadBroadcastReciever.class.getSimpleName();

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

                ContentValues values = new ContentValues();
                values.put( EpisodeConstants.FIELD_DOWNLOADED, 1 );
                int updated = context.getContentResolver().update( ContentUris.withAppendedId( EpisodeConstants.CONTENT_URI, episodeId ), values, null, null );
                Log.i( TAG, "onRecieve : updated=" + updated );

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

}
