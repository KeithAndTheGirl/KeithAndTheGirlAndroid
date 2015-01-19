package com.keithandthegirl.app.services.media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.ui.episode.EpisodeActivity;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Service that handles media playback. This is the Service through which we perform all the media
 * handling in our application. It waits for Intents, which signal the service to perform specific operations: Play, Pause,
 * Rewind, Skip, etc.
 */
public class MediaService extends Service implements OnCompletionListener, OnPreparedListener, OnErrorListener, AudioFocusable {
    private final static String TAG = MediaService.class.getSimpleName();

    private final IBinder mBinder = new MediaServiceBinder();
    private MediaServiceEventListener mMediaServiceEventListener = null;
    // The id of the episode to play
    public static final String EXTRA_EPISODE_ID = "com.keithandthegirl.app.services.media.extra.EPISODE_ID";

    public static final String EXTRA_SEEK_POSITION = "com.keithandthegirl.app.services.media.extra.SEEK_POSITION";

    // These are the Intent actions that we are prepared to handle. Notice that the fact these
    // constants exist in our class is a mere convenience: what really defines the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for our service in
    // AndroidManifest.xml.
    public static final String ACTION_TOGGLE_PLAYBACK = "com.keithandthegirl.app.services.media.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.keithandthegirl.app.services.media.action.PLAY";
    public static final String ACTION_PAUSE = "com.keithandthegirl.app.services.media.action.PAUSE";
    public static final String ACTION_STOP = "com.keithandthegirl.app.services.media.action.STOP";
    public static final String ACTION_SKIP = "com.keithandthegirl.app.services.media.action.SKIP";
    public static final String ACTION_FASTFORWARD = "com.keithandthegirl.app.services.media.action.FASTFORWARD";
    public static final String ACTION_REWIND = "com.keithandthegirl.app.services.media.action.REWIND";
    public static final String ACTION_PREVIOUS = "com.keithandthegirl.app.services.media.action.PREVIOUS";
    public static final String ACTION_SEEK = "com.keithandthegirl.app.services.media.action.SEEK";
    public static final String ACTION_URL = "com.keithandthegirl.app.services.media.action.URL";
    public static final String ACTION_STATUS = "com.keithandthegirl.app.services.media.action.STATUS";

    public static final String EVENT_STATUS = "com.keithandthegirl.app.services.media.event.STATUS";

    // The state of the media player
    public static final String EXTRA_STATE = "com.keithandthegirl.app.services.media.action.STATE";
    public static final String EXTRA_CURRENT_POSITION = "com.keithandthegirl.app.services.media.action.CURRENT_POSITION";
    public static final String EXTRA_PLAYED_POSITION = "com.keithandthegirl.app.services.media.action.PLAYED_POSITION";
    public static final String EXTRA_DURATION = "com.keithandthegirl.app.services.media.action.DURATION";
    public static final String EXTRA_TITLE = "com.keithandthegirl.app.services.media.action.TITLE";

    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;

    // The amount if time (in ms) to fast forward and rewind
    private static final int REW_JUMP_MILLISEC = 10000;
    private static final int FF_JUMP_MILLISEC = 30000;

    // our media player
    MediaPlayer mPlayer = null;

    // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;

    // indicates the state our service:
    public enum State {
        NONE,       // no state yet. used to hide playback bar
        RETRIEVING, // the MediaRetriever is retrieving music
        STOPPED,    // media player is stopped and not prepared to play
        PREPARING,  // media player is preparing...
        PLAYING,    // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        PAUSED      // playback paused (media player ready!)
    }

    State mState = State.NONE;

    // if in Retrieving mode, this flag indicates whether we should start playing immediately
    // when we are ready or not.
    boolean mStartPlayingAfterRetrieve = false;

    // if mStartPlayingAfterRetrieve is true, this variable indicates the URL that we should
    // start playing when we are ready. If null, we should play a random song from the device
    Uri mWhatToPlayAfterRetrieve = null;

    enum PauseReason {
        USER_REQUEST,  // paused by user request
        FOCUS_LOSS,    // paused because of audio focus loss
    }

    // why did we pause? (only relevant if mState == State.Paused)
    PauseReason mPauseReason = PauseReason.USER_REQUEST;

    // do we have audio focus?
    enum AudioFocus {
        NO_FOCUS_NO_DUCK,    // we don't have audio focus, and can't duck
        NO_FOCUS_CAN_DUCK,   // we don't have focus, but can play at a low volume ("ducking")
        FOCUSED           // we have full audio focus
    }

    AudioFocus mAudioFocus = AudioFocus.NO_FOCUS_NO_DUCK;

    Long mEpisodeId;
    EpisodeInfoHolder mEpisodeHolder;

    // title of the song we are currently playing
//    String mSongTitle = "";

    // whether the song we are playing is streaming from the network
    boolean mIsStreaming = false;

    // Wifi lock that we hold when streaming files from the internet, in order to prevent the
    // device from shutting off the Wifi radio
    WifiLock mWifiLock;

    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    final int NOTIFICATION_ID = 2;

    // our RemoteControlClient object, which will use remote control APIs available in
    // SDK level >= 14, if they're available.
    RemoteControlClient mRemoteControlClientCompat;

    // Dummy album art we will pass to the remote control (if the APIs are available).
    Bitmap mDummyAlbumArt;

    // The component name of MediaIntentReceiver, for use with media button and remote control
    // APIs
    ComponentName mMediaButtonReceiverComponent;
    AudioManager mAudioManager;
    NotificationManager mNotificationManager;
    Notification mNotification = null;

    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayerIfNeeded() {

        if (mPlayer == null) {
            mPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        } else
            mPlayer.reset();
    }

    @Override
    public void onCreate() {
        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.FOCUSED; // no focus feature, so we always "have" audio focus

        mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        mMediaButtonReceiverComponent = new ComponentName(this, MediaIntentReceiver.class);
    }

    /**
     * Called when we receive an Intent. When we receive an intent sent to us via startService(),
     * this is the method that gets called. So here we react appropriately depending on the
     * Intent's action, which specifies what is being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.i(TAG, "onStartCommand : action=" + action);

        switch (action) {
            case ACTION_TOGGLE_PLAYBACK:
                processTogglePlaybackRequest();
                break;
            case ACTION_PLAY:
                processPlayRequest();
                break;
            case ACTION_PAUSE:
                processPauseRequest();
                break;
            case ACTION_FASTFORWARD:
                processFastForwardRequest();
                break;
            case ACTION_SKIP:
                processSkipRequest();
                break;
            case ACTION_STOP:
                processStopRequest();
                break;
            case ACTION_REWIND:
                processRewindRequest();
                break;
            case ACTION_PREVIOUS:
                processPreviousRequest();
                break;
            case ACTION_SEEK:
                processSeekRequest(intent);
                break;
            case ACTION_URL:
                processAddRequest(intent);
                break;
            case ACTION_STATUS:
                processStatusRequest();
                break;
        }

        return START_NOT_STICKY; // Means we started the service, but don't want it to
        // restart in case it's killed.
    }

    public void setMediaServiceEventListener(MediaServiceEventListener mediaServiceEventListener) {
        mMediaServiceEventListener = mediaServiceEventListener;
    }

    void processSeekRequest(Intent intent) {
        if (null != mPlayer) {
            int seekPosition = intent.getIntExtra(EXTRA_SEEK_POSITION, -1);

            processSeekRequest(seekPosition);

            processStatusRequest();
        }
    }

    public void processSeekRequest(int seekPosition) {
        mPlayer.seekTo(seekPosition);
        updateLastPlayed(seekPosition);
    }

    public void processStatusRequest() {
        if (null == mEpisodeId || mEpisodeId.longValue() <= 0) {
            return;
        }

        Intent broadcast = new Intent(EVENT_STATUS);
        broadcast.putExtra(EXTRA_EPISODE_ID, mEpisodeId);
        broadcast.putExtra(EXTRA_TITLE, mEpisodeHolder.getShowPrefix() + " : " + mEpisodeHolder.getEpisodeNumber() + " - " + mEpisodeHolder.getEpisodeTitle());
        broadcast.putExtra(EXTRA_STATE, mState.name());

        if (null != mPlayer && (mState.equals(State.PLAYING) || mState.equals(State.PAUSED))) {

            final int HOUR = 60 * 60 * 1000;
            final int MINUTE = 60 * 1000;
            final int SECOND = 1000;

            int durationInMillis = mPlayer.getDuration();
            int curVolume = mPlayer.getCurrentPosition();

            int durationHour = durationInMillis / HOUR;
            int durationMint = (durationInMillis % HOUR) / MINUTE;
            int durationSec = (durationInMillis % MINUTE) / SECOND;

            int currentHour = curVolume / HOUR;
            int currentMint = (curVolume % HOUR) / MINUTE;
            int currentSec = (curVolume % MINUTE) / SECOND;

            String played = "00:00", duration = "00:00";

            if (durationHour > 0) {
                played = String.format("%02d:%02d:%02d", currentHour, currentMint, currentSec);
                duration = String.format("%02d:%02d:%02d", durationHour, durationMint, durationSec);
            } else {
                played = String.format("%02d:%02d", currentMint, currentSec);
                duration = String.format("%02d:%02d", durationMint, durationSec);
            }

            broadcast.putExtra(EXTRA_CURRENT_POSITION, mPlayer.getCurrentPosition());
            broadcast.putExtra(EXTRA_PLAYED_POSITION, played);
            broadcast.putExtra(EXTRA_DURATION, duration);
        }

        sendBroadcast(broadcast);
    }

    public void processTogglePlaybackRequest() {
        if (mState == State.PAUSED || mState == State.STOPPED) {
            processPlayRequest();
        } else {
            processPauseRequest();
        }
    }

    public void processPlayRequest() {
        if (null == mPlayer) {
            return;
        }

        if (mState == State.RETRIEVING) {
            // If we are still retrieving media, just set the flag to start playing when we're
            // ready
            mWhatToPlayAfterRetrieve = null; // play a random song
            mStartPlayingAfterRetrieve = true;

            return;
        }

        tryToGetAudioFocus();

        // actually play the song
        if (mState == State.STOPPED) {
            // If we're stopped, just go ahead to the next song and start playing
            playEpisode();
        } else if (mState == State.PAUSED) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = State.PLAYING;
            setUpAsForeground(mEpisodeHolder.getEpisodeTitle() + " (playing)");
            configAndStartMediaPlayer();
        }

        // Tell any remote controls that our playback state is 'playing'.
        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }

        processStatusRequest();
    }

    public void processPauseRequest() {
        if (null == mPlayer) {
            return;
        }

        if (mState == State.RETRIEVING) {
            // If we are still retrieving media, clear the flag that indicates we should start
            // playing when we're ready
            mStartPlayingAfterRetrieve = false;

//            Log.d(TAG, "processPauseRequest : exit, retrieving");
            return;
        }

        if (mState == State.PLAYING) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.PAUSED;
            mPlayer.pause();
            relaxResources(false); // while paused, we always retain the MediaPlayer
            // do not give up audio focus
        }

        // Tell any remote controls that our playback state is 'paused'.
        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }

        processStatusRequest();
    }

    public void processPreviousRequest() {
        if (null == mPlayer) {
            return;
        }

        if (mState == State.PLAYING || mState == State.PAUSED)
            mPlayer.seekTo(0);

        processStatusRequest();
    }

    public void processRewindRequest() {
        if (null == mPlayer) {
            return;
        }

        if (mState == State.PLAYING || mState == State.PAUSED) {

            int currentPosition = mPlayer.getCurrentPosition() - REW_JUMP_MILLISEC;

            if (currentPosition >= 0) {
                mPlayer.seekTo(currentPosition);
            } else {
                mPlayer.seekTo(0);
            }
        }

        processStatusRequest();
    }

    public void processFastForwardRequest() {
        if (null == mPlayer) {
            return;
        }

        if (mState == State.PLAYING || mState == State.PAUSED) {
            tryToGetAudioFocus();

            int currentPosition = mPlayer.getCurrentPosition() + FF_JUMP_MILLISEC;

            if (currentPosition < mPlayer.getDuration()) {
                mPlayer.seekTo(currentPosition);
            } else {
                mPlayer.seekTo(mPlayer.getDuration());
            }
        }

        processStatusRequest();
    }

    public void processSkipRequest() {
        if (mState == State.PLAYING || mState == State.PAUSED) {
            tryToGetAudioFocus();
//            playNextSong(null);
        }

        processStatusRequest();
    }

    public void processStopRequest() {
        processStopRequest(false);
    }

    public void processStopRequest(boolean force) {
        if (null == mPlayer) {
            return;
        }

        if (mState == State.PLAYING || mState == State.PAUSED || force) {
            mState = State.STOPPED;

            // let go of all resources...
            relaxResources(true);
            giveUpAudioFocus();

            // Tell any remote controls that our playback state is 'paused'.
            if (mRemoteControlClientCompat != null) {
                mRemoteControlClientCompat
                        .setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
            }

            processStatusRequest();

//            Log.d(TAG, "processStopRequest : exit, stopping service");
            // service is no longer necessary. Will be started again if needed.
            if (force) {
                stopSelf();
            }
        }
    }

    /**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    void relaxResources(boolean releaseMediaPlayer) {
        timerHandler.removeCallbacks(timerRunnable);

        // stop being a foreground service
        stopForeground(true);

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }

        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) mWifiLock.release();
    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.FOCUSED && mAudioFocusHelper != null
                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NO_FOCUS_NO_DUCK;
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NO_FOCUS_NO_DUCK) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (mPlayer.isPlaying()) mPlayer.pause();

//            Log.d(TAG, "configAndStartMediaPlayer : exit, no focus no duck");
            return;
        } else if (mAudioFocus == AudioFocus.NO_FOCUS_CAN_DUCK)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud

        if (mEpisodeHolder.getEpisodeLastPlayed() > 0) {
            mPlayer.seekTo(mEpisodeHolder.getEpisodeLastPlayed());
        }

        if (!mPlayer.isPlaying()) {
            mPlayer.start();
        }

        timerHandler.postDelayed(timerRunnable, 0);
    }

    public void processAddRequest(Intent intent) {
        if (mState == State.PLAYING || mState == State.PAUSED) {

            processStopRequest();

        }

        mEpisodeId = intent.getLongExtra(EXTRA_EPISODE_ID, -1);
        mEpisodeHolder = EpisodeInfoHolder.loadEpisode(this, mEpisodeId);

        mState = State.STOPPED;

        tryToGetAudioFocus();
        playEpisode();
    }

    public EpisodeInfoHolder getEpisode() {
        return mEpisodeHolder;
    }

    public State getState() {
        return mState;
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.FOCUSED && mAudioFocusHelper != null
                && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.FOCUSED;
    }

    /**
     * Starts playing the next song. If manualUrl is null, the next song will be randomly selected
     * from our Media Retriever (that is, it will be a random song in the user's device). If
     * manualUrl is non-null, then it specifies the URL or path to the song that will be played
     * next.
     */
    void playEpisode() {
        mState = State.STOPPED;
        relaxResources(false); // release everything except MediaPlayer

        if (null == mEpisodeHolder) {
            processStopRequest(true); // stop everything!

            return;
        }

        mIsStreaming = !mEpisodeHolder.isEpisodeDownloaded(); // playing a locally available song

        try {
            createMediaPlayerIfNeeded();

            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            if (mEpisodeHolder.isEpisodeDownloaded()) {
                File externalFile = new File(getExternalFilesDir(null), mEpisodeHolder.getEpisodeFilename());
                mPlayer.setDataSource(Uri.fromFile(externalFile).toString());
            } else {
                mPlayer.setDataSource(mEpisodeHolder.getEpisodeFileUrl());
            }

            mState = State.PREPARING;
            setUpAsForeground(mEpisodeHolder.getEpisodeTitle() + " (loading)");

            // Use the media button APIs (if available) to register ourselves for media button
            // events
            MediaButtonHelper.registerMediaButtonEventReceiverCompat(mAudioManager, mMediaButtonReceiverComponent);

            // Use the remote control APIs (if available) to set the playback state
            if (mRemoteControlClientCompat == null) {
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                mediaButtonIntent.setComponent(mMediaButtonReceiverComponent);
                mRemoteControlClientCompat = new RemoteControlClient(
                        PendingIntent.getBroadcast(this /*context*/,
                                0 /*requestCode, ignored*/, mediaButtonIntent /*intent*/, 0 /*flags*/));
                RemoteControlHelper.registerRemoteControlClient(mAudioManager, mRemoteControlClientCompat);
            }

            mRemoteControlClientCompat.setPlaybackState(
                    RemoteControlClient.PLAYSTATE_PLAYING);

            mRemoteControlClientCompat.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT);

            // Update the remote controls
            mRemoteControlClientCompat.editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, mEpisodeHolder.getShowName())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, mEpisodeHolder.getShowPrefix() + " : " + mEpisodeHolder.getEpisodeNumber())
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, mEpisodeHolder.getEpisodeTitle())
                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, mEpisodeHolder.getEpisodeLength())
                            // TODO: fetch real item artwork
                    .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt)
                    .apply();

            // starts preparing the media player in the background. When it's done, it will call
            // our OnPreparedListener (that is, the onPrepared() method on this class, since we set
            // the listener to 'this').
            //
            // Until the media player is prepared, we *cannot* call start() on it!
            mPlayer.prepareAsync();

            // If we are streaming from the internet, we want to hold a Wifi lock, which prevents
            // the Wifi radio from going to sleep while the song is playing. If, on the other hand,
            // we are *not* streaming, we want to release the lock if we were holding it before.
            if (mIsStreaming) mWifiLock.acquire();
            else if (mWifiLock.isHeld()) mWifiLock.release();
        } catch (IOException ex) {
            Log.e(TAG, "IOException playing next song: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Called when media player is done playing current song.
     */
    public void onCompletion(MediaPlayer player) {
        // The media player finished playing the current song, so we go ahead and start the next.
//        playNextSong(null);
        processStopRequest(true);
    }

    /**
     * Called when media player is done preparing.
     */
    public void onPrepared(MediaPlayer player) {
        // The media player is done preparing. That means we can start playing!
        mState = State.PLAYING;
        updateNotification(mEpisodeHolder.getEpisodeTitle() + " (playing)");
        configAndStartMediaPlayer();

        processStatusRequest();
    }

    /**
     * Updates the notification.
     */
    void updateNotification(String text) {
        Intent episodeIntent = new Intent(getApplicationContext(), EpisodeActivity.class);
        episodeIntent.putExtra(EpisodeActivity.ARG_EPISODE_KEY, mEpisodeId);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(EpisodeActivity.class);
        stackBuilder.addNextIntent(episodeIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotification = new NotificationCompat
                .Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Playing " + mEpisodeHolder.getShowPrefix() + " : " + mEpisodeHolder.getEpisodeNumber())
                .setContentText(mEpisodeHolder.getEpisodeTitle())
                .setOngoing(true)
                .setContentIntent(resultPendingIntent)
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

        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing
     * something the user is actively aware of (such as playing music), and must appear to the
     * user as a notification. That's why we create the notification here.
     */
    void setUpAsForeground(String text) {
        Intent episodeIntent = new Intent(getApplicationContext(), EpisodeActivity.class);
        episodeIntent.putExtra(EpisodeActivity.ARG_EPISODE_KEY, mEpisodeId);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(EpisodeActivity.class);
        stackBuilder.addNextIntent(episodeIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotification = new NotificationCompat
                .Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Playing " + mEpisodeHolder.getShowPrefix() + " : " + mEpisodeHolder.getEpisodeNumber())
                .setContentText(mEpisodeHolder.getEpisodeTitle())
                .setOngoing(true)
                .setContentIntent(resultPendingIntent)
//                .addAction(
//                        R.drawable.,
//                        "New",
//                        getPendingIntent(this.getShowAdvertisingmEpisodeHolder.getEpisodeIntent(this.getLaunchNewGmailEmailIntent(labelData)))
//                )
//                .addAction(
//                        R.drawable.ic_menu_preferences,
//                        getString(R.string.notification_action_settings_title),
//                        getPendingIntent(this.getLaunchSettingsIntent()))
                .build();

        startForeground(NOTIFICATION_ID, mNotification);
    }

    /**
     * Called when there's an error playing media. When this happens, the media player goes to
     * the Error state. We warn the user about the error and reset the media player.
     */
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "KATG player error! Resetting.", Toast.LENGTH_SHORT).show();

        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));
        mState = State.STOPPED;
        relaxResources(true);
        giveUpAudioFocus();

        return true; // true indicates we handled the error
    }

    public void onGainedAudioFocus() {
//        Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();

        mAudioFocus = AudioFocus.FOCUSED;

        // restart media player with new focus settings
        if (mState == State.PLAYING)
            configAndStartMediaPlayer();

        processStatusRequest();
    }

    public void onLostAudioFocus(boolean canDuck) {
//        Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" : "no duck"), Toast.LENGTH_SHORT).show();

        mAudioFocus = canDuck ? AudioFocus.NO_FOCUS_CAN_DUCK : AudioFocus.NO_FOCUS_NO_DUCK;

        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer.isPlaying())
            configAndStartMediaPlayer();

        processStatusRequest();
    }

    @Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        mState = State.STOPPED;
        relaxResources(true);
        giveUpAudioFocus();
    }

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        private final Date started = new Date();
        @Override
        public void run() {
            // persist current position
            updateLastPlayed(mPlayer.getCurrentPosition());

            // create and send broadcast message for status update
            processStatusRequest();

            // if a updateListener has been added from a local bind call status update
            if (mMediaServiceEventListener != null) {
                mState = State.PLAYING;
                mMediaServiceEventListener.onStatusUpdate(mEpisodeHolder);
            }

            // set timer to run again
            timerHandler.postDelayed(this, 1000);
        }
    };

    void updateLastPlayed(int currentPosition) {
        if (mEpisodeHolder == null) {
            return;
        }

        mEpisodeHolder.setEpisodeLastPlayed(currentPosition);

        ContentValues values = new ContentValues();
        values.put(EpisodeConstants.FIELD_PLAYED, 1);
        values.put(EpisodeConstants.FIELD_LASTPLAYED, currentPosition);

        getContentResolver().update(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, mEpisodeId), values, null, null);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public class MediaServiceBinder extends Binder {
        public MediaService getMediaService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaService.this;
        }
    }

    public interface MediaServiceEventListener {
        void onStatusUpdate(EpisodeInfoHolder episodeInfoHolder);
    }
}