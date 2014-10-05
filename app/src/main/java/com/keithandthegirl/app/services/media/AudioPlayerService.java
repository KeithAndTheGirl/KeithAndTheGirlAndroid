package com.keithandthegirl.app.services.media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.webkit.URLUtil;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.ui.episode.EpisodeActivity;
import com.keithandthegirl.app.ui.episode.EpisodeInfoHolder;

import java.io.File;
import java.io.IOException;

public class AudioPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public static final String TAG = AudioPlayerService.class.getName();

    public static final int NOTIFICATION_ID = 2;
    public static final String EXTRA_EPISODE_ID = "EPISODE_ID";
    public static final String EXTRA_CURRENT_POSITION = "CURRENT_POSITION";
    public static final String EXTRA_SEEK_POSITION = "SEEK_POSITION";
    public static final String EXTRA_IS_PLAYING = "IS_PLAYING";
    public static final String ACTION_PLAY = "com.keithandthegirl.app.services.media.AudioPlayerService.action.PLAY";
    public static final String ACTION_STOP = "com.keithandthegirl.app.services.media.AudioPlayerService.action.STOP";
    public static final String ACTION_PAUSE = "com.keithandthegirl.app.services.media.AudioPlayerService.action.PAUSE";
    public static final String ACTION_SEEK = "com.keithandthegirl.app.services.media.AudioPlayerService.action.SEEK";
    public static final String ACTION_FF = "com.keithandthegirl.app.services.media.media.AudioPlayerService.action.FF";
    public static final String ACTION_REW = "com.keithandthegirl.app.services.media.AudioPlayerService.action.REW";
    public static final String ACTION_IS_PLAYING = "com.keithandthegirl.app.services.media.AudioPlayerService.action.IS_PLAYING";

    public static final String EVENT_STATUS = "com.keithandthegirl.app.services.media.AudioPlayerService.action.STATUS";

    private static final String WIFI_LOCK_NAME = "com.keithandthegirl.app.services.media.AudioPlayerService.WIFI_LOCK";

    private static final int REW_JUMP_MILLISEC = 10000;
    private static final int FF_JUMP_MILLISEC = 30000;

    private WifiManager.WifiLock mWifiLock;
    private MediaPlayer mMediaPlayer = null;

    private Uri mEpisodeUri;
    private long mEpisodeId;
    private EpisodeInfoHolder mEpisodeInfo;

    private NotificationManager mNotificationManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE );
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(ACTION_PLAY)) {
            mEpisodeId = intent.getLongExtra(EXTRA_EPISODE_ID, -1);
            loadEpisode();
            this.prepareMediaPlayer();
        } else if (intent.getAction().equals(ACTION_STOP)) {
            stopSelf();
            mNotificationManager.cancel( NOTIFICATION_ID );
        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            pauseMediaPlayer();
        } else if (intent.getAction().equals(ACTION_SEEK)) {
            int position = intent.getIntExtra(EXTRA_SEEK_POSITION, -1);
            if(position != -1) seekMediaPlayer(position);
        } else if (intent.getAction().equals(ACTION_FF)) {
            ffMediaPlayer();
        } else if (intent.getAction().equals(ACTION_REW)) {
            rewMediaPlayer();
        } else if (intent.getAction().equals(ACTION_IS_PLAYING)) {
            isPlaying();
        }

        return START_NOT_STICKY;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {

        player.start();
        if( mEpisodeInfo.getEpisodeLastPlayed() > 0 ) {
            mMediaPlayer.seekTo( mEpisodeInfo.getEpisodeLastPlayed() );
        }

        updateLastPlayed();

        timerHandler.postDelayed(timerRunnable, 0);
    }

    @Override
    public void onDestroy() {

        mNotificationManager.cancel( NOTIFICATION_ID );

        if (mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if(mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
            mWifiLock = null;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        return true;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification() {

        if( null == mEpisodeInfo ) {
            return;
        }

        Intent episodeIntent = new Intent( getApplicationContext(), EpisodeActivity.class );
        episodeIntent.putExtra( EpisodeActivity.EPISODE_KEY, mEpisodeId );

        TaskStackBuilder stackBuilder = TaskStackBuilder.create( getApplicationContext() );
        stackBuilder.addParentStack( EpisodeActivity.class );
        stackBuilder.addNextIntent( episodeIntent );
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );

        Notification notification = new NotificationCompat
            .Builder( getApplicationContext() )
                .setSmallIcon( R.drawable.ic_launcher )
                .setContentTitle( "Playing " + mEpisodeInfo.getShowPrefix() + " : " + mEpisodeInfo.getEpisodeNumber() )
                .setContentText( mEpisodeInfo.getEpisodeTitle() )
                .setOngoing( true )
                .setContentIntent( resultPendingIntent )
//                .addAction(
//                        R.drawable.,
//                        "New",
//                        getPendingIntent(this.getShowAdvertisingIntent(this.getLaunchNewGmailEmailIntent(labelData)))
//                )
//                .addAction(
//                        R.drawable.ic_menu_preferences,
//                        getString(R.string.notification_action_settings_title),
//                        getPendingIntent(this.getLaunchSettingsIntent()))
                .build();

        startForeground( NOTIFICATION_ID, notification );

        mNotificationManager.notify( NOTIFICATION_ID, notification );
    }

    private void loadEpisode() {

        if( mEpisodeId == -1 ) {
            return;
        }

        EpisodeInfoHolder episodeHolder = null;

        Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, mEpisodeId), null, null, null, null);
        if (cursor.moveToNext()) {
            episodeHolder = new EpisodeInfoHolder();
            episodeHolder.setEpisodeNumber(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_NUMBER)));
            episodeHolder.setEpisodeTitle(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_TITLE)));
//            episodeHolder.setEpisodePreviewUrl(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_PREVIEWURL)));
            episodeHolder.setEpisodeFileUrl(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_FILEURL)));
            episodeHolder.setEpisodeFilename(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_FILENAME)));
            episodeHolder.setEpisodeLength(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_LENGTH)));
            episodeHolder.setEpisodeFileSize(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_FILESIZE)));
//            episodeHolder.setEpisodeType(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_TYPE)));
//            episodeHolder.setEpisodePublic(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_PUBLIC)) == 1);
//            episodeHolder.setEpisodePosted(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_POSTED)));
//            episodeHolder.setEpisodeDownloadId(cursor.getLong(cursor.getColumnIndex(EpisodeConstants.FIELD_DOWNLOAD_ID)));
//            episodeHolder.setEpisodeDownloaded(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_DOWNLOADED)) == 1);
            episodeHolder.setEpisodePlayed(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_PLAYED)));
            episodeHolder.setEpisodeLastPlayed(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_LASTPLAYED)));
//            episodeHolder.setShowNameId(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_SHOWNAMEID)));
//            episodeHolder.setEpisodeDetailNotes(cursor.getString(cursor.getColumnIndex(DetailConstants.TABLE_NAME + "_" + DetailConstants.FIELD_NOTES)));
//            episodeHolder.setEpisodeDetailForumUrl(cursor.getString(cursor.getColumnIndex(DetailConstants.TABLE_NAME + "_" + DetailConstants.FIELD_FORUMURL)));
//            episodeHolder.setShowName(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_NAME)));
            episodeHolder.setShowPrefix(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_PREFIX)));
//            episodeHolder.setShowVip(cursor.getInt(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_VIP)) == 1 ? true : false);
//            episodeHolder.setShowCoverImageUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_COVERIMAGEURL_200)));
//            episodeHolder.setShowForumUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_FORUMURL)));
//            episodeHolder.setGuestNames(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_GUEST_NAMES)));

//            String guestImages = cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_GUEST_IMAGES));
//            if(null != guestImages && !"".equals(guestImages)) {
//                String[] images = guestImages.split(",");
//                episodeHolder.setEpisodeGuestImages(Arrays.asList(images));
//            } else {
//                episodeHolder.setEpisodeGuestImages(Collections.EMPTY_LIST);
//            }

        }
        cursor.close();

        if( null != episodeHolder ) {

            Uri episodeUri = null;
            if( episodeHolder.isEpisodeDownloaded() ) {
                Log.i( TAG, "loading from file");

                File externalFile = new File(getExternalFilesDir(null), episodeHolder.getEpisodeFilename());
                episodeUri = Uri.fromFile( externalFile );
            } else {
                Log.i( TAG, "streaming from url");

                episodeUri = Uri.parse( episodeHolder.getEpisodeFileUrl());
            }

            mEpisodeUri = episodeUri;
        }

        mEpisodeInfo = episodeHolder;
    }

    private void isPlaying() {

        boolean playing = false;
        if( mMediaPlayer == null ) {
            playing = false;
        } else {
            playing = mMediaPlayer.isPlaying();
        }

        int position = 0;
        if( null != mEpisodeInfo ) {
            position = mEpisodeInfo.getEpisodeLastPlayed();
        }

        Intent broadcast = new Intent( EVENT_STATUS );
        broadcast.putExtra(EXTRA_EPISODE_ID, mEpisodeId);
        broadcast.putExtra(EXTRA_CURRENT_POSITION, position);
        broadcast.putExtra(EXTRA_IS_PLAYING, playing );
        sendBroadcast( broadcast );

    }

    private void updateLastPlayed() {

        if( mMediaPlayer == null ) {
            return;
        }

        if( mEpisodeInfo == null ) {
            return;
        }

        mEpisodeInfo.setEpisodeLastPlayed( mMediaPlayer.getCurrentPosition() );

        ContentValues values = new ContentValues();
        values.put(EpisodeConstants.FIELD_PLAYED, 1 );
        values.put(EpisodeConstants.FIELD_LASTPLAYED, mEpisodeInfo.getEpisodeLastPlayed() );

        getContentResolver().update(ContentUris.withAppendedId( EpisodeConstants.CONTENT_URI, mEpisodeId ), values, null, null );

        Intent broadcast = new Intent( EVENT_STATUS );
        broadcast.putExtra(EXTRA_EPISODE_ID, mEpisodeId);
        broadcast.putExtra(EXTRA_CURRENT_POSITION, mEpisodeInfo.getEpisodeLastPlayed());
        sendBroadcast( broadcast );

    }

    private void prepareMediaPlayer() {

        //reset if media player exists
        if(mMediaPlayer != null) {
            mMediaPlayer.reset();
        } else {
            mMediaPlayer = new MediaPlayer();
        }

        try {

            //prepare media payer
            mMediaPlayer.setDataSource(this, mEpisodeUri);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            //if a remote file acquire a wifi wake lock
            if(!URLUtil.isFileUrl(mEpisodeUri.toString())){
                mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_NAME);
                mWifiLock.acquire();
            }

            mMediaPlayer.prepareAsync();

            createNotification();

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    private void pauseMediaPlayer() {

        if(mMediaPlayer == null) return;

        if(!mMediaPlayer.isPlaying()) return;

        try{
            mNotificationManager.cancel( NOTIFICATION_ID );
            stopForeground( true );

            updateLastPlayed();

            mMediaPlayer.stop();
            mMediaPlayer.reset();

            mEpisodeId = -1;
            mEpisodeUri = null;
            mEpisodeInfo = null;

            timerHandler.removeCallbacks(timerRunnable);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    private void seekMediaPlayer(int position){
        if(mMediaPlayer == null) return;

        try{
            mMediaPlayer.seekTo(position);

            updateLastPlayed();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void ffMediaPlayer(){
        if(mMediaPlayer == null) return;

        try{
            int currentPosition = mMediaPlayer.getCurrentPosition() + FF_JUMP_MILLISEC;
            Log.d(TAG, "ffMediaPlayer() - " + currentPosition );

            if(currentPosition < mMediaPlayer.getDuration()) mMediaPlayer.seekTo(currentPosition);

            updateLastPlayed();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void rewMediaPlayer(){
        if(mMediaPlayer == null) return;

        try{
            int currentPosition = mMediaPlayer.getCurrentPosition() - REW_JUMP_MILLISEC;
            Log.d(TAG, "rewMediaPlayer() - " + currentPosition );

            if(currentPosition >= 0) mMediaPlayer.seekTo(currentPosition);

            updateLastPlayed();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            updateLastPlayed();

            timerHandler.postDelayed(this, 1000);
        }
    };

}
