package com.keithandthegirl.app.ui.episodesimpler;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.services.media.AudioPlayerService;
import com.keithandthegirl.app.sync.SyncAdapter;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.episodesimpler.gallery.EpisodeImageGalleryFragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class EpisodeActivity extends AbstractBaseActivity implements EpisodeFragment.EpisodeEventListener, OnClickListener {
    public static final String EPISODE_KEY = "EPISODE_KEY";
    private static final String TAG = EpisodeActivity.class.getSimpleName();
    private long mEpisodeId;
    private LinearLayout mPlayerControls;
    private Button mPlayButton, mPauseButton, mBackButton, mSkipButton;

    private EpisodeInfoHolder mEpisodeInfoHolder;

    private MenuItem mDownloadMenuItem, mDeleteMenuItem;

    private DownloadManager mDownloadManager;

    private PlaybackBroadcastReceiver mPlaybackBroadcastReceiver = new PlaybackBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodesimpler);

        Bundle extras = getIntent().getExtras();
        if (extras.containsKey(EPISODE_KEY)) {
            mEpisodeId = extras.getLong(EPISODE_KEY);
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, EpisodeFragment.newInstance(mEpisodeId))
                    .addToBackStack(null)
                    .commit();
        }

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        mPlayerControls = (LinearLayout) findViewById( R.id.playbackLayout);
        mPlayButton = (Button) findViewById(R.id.play);
        mPlayButton.setEnabled(false);
        mPlayButton.setOnClickListener(this);

        mPauseButton = (Button) findViewById(R.id.pause);
        mPauseButton.setEnabled(false);
        mPauseButton.setOnClickListener(this);

        mBackButton = (Button) findViewById(R.id.back);
        mBackButton.setEnabled(false);
        mBackButton.setOnClickListener(this);

        mSkipButton = (Button) findViewById(R.id.skip);
        mSkipButton.setEnabled(false);
        mSkipButton.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter playbackBroadcastIntentFilter = new IntentFilter(AudioPlayerService.EVENT_STATUS);
        registerReceiver(mPlaybackBroadcastReceiver, playbackBroadcastIntentFilter);

        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_IS_PLAYING);
        startService(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if( null != mPlaybackBroadcastReceiver ) {
            unregisterReceiver(mPlaybackBroadcastReceiver);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.episode, menu);

        mDownloadMenuItem = menu.findItem( R.id.action_download );
        mDeleteMenuItem = menu.findItem( R.id.action_delete );

        swapMenuItems();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch( item.getItemId() ) {

            case R.id.action_download :
                queueDownload();
                break;

            case R.id.action_delete :
                deleteEpisode();
                break;

            case R.id.action_settings :
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEpisodeLoaded(final EpisodeInfoHolder episodeInfoHolder) {

        mEpisodeInfoHolder = episodeInfoHolder;

        getActionBar().setTitle( mEpisodeInfoHolder.getShowName() );

        if( mEpisodeInfoHolder.isEpisodePublic() ) {
            mPlayerControls.setVisibility( View.VISIBLE );
            mPlayButton.setEnabled(true);
        } else {
            mPlayerControls.setVisibility( View.GONE );
        }
        // TODO Enable UI better now that we have episodeId
        // TODO also need to save it for config change

    }

    @Override
    public void onShowImageClicked(final int position, final List<String> imageUrls) {
        String[] strings = imageUrls.toArray(new String[imageUrls.size()]);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, EpisodeImageGalleryFragment.newInstance(position, strings), EpisodeImageGalleryFragment.STACK_NAME)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onClick(View v) {

        Intent intent = null;

        switch( v.getId() ) {

            case R.id.play :
                intent = new Intent(this, AudioPlayerService.class);
                intent.setAction(AudioPlayerService.ACTION_PLAY);
                intent.putExtra(AudioPlayerService.EXTRA_EPISODE_ID, mEpisodeId);

                mPlayButton.setVisibility( View.GONE );
                mPauseButton.setVisibility( View.VISIBLE );
                mPauseButton.setEnabled(true);
                mBackButton.setEnabled(true);
                mSkipButton.setEnabled(true);
                break;

            case R.id.pause :
                intent = new Intent(this, AudioPlayerService.class);
                intent.setAction(AudioPlayerService.ACTION_PAUSE);

                mPlayButton.setVisibility( View.VISIBLE );
                mPauseButton.setVisibility( View.GONE );
                mPauseButton.setEnabled(false);
                mBackButton.setEnabled(false);
                mSkipButton.setEnabled(false);
                break;

            case R.id.back :
                intent = new Intent(this, AudioPlayerService.class);
                intent.setAction(AudioPlayerService.ACTION_REW);
                break;

            case R.id.skip :
                intent = new Intent(this, AudioPlayerService.class);
                intent.setAction(AudioPlayerService.ACTION_FF);
                break;

        }

        if(intent != null) {

            intent.putExtra(AudioPlayerService.EXTRA_EPISODE_ID, mEpisodeId);

            startService(intent);

        }

    }

    private void swapMenuItems() {

        if( null == mEpisodeInfoHolder ) {

            mDownloadMenuItem.setVisible( false );
            mDownloadMenuItem.setEnabled( false );
            mDeleteMenuItem.setVisible( false );
            mDeleteMenuItem.setEnabled( false );

            return;
        }

        boolean isDownloadedOrDownloading = mEpisodeInfoHolder.isEpisodeDownloaded() || isDownloading();
        Log.i( TAG, "swapMenuItems : isDownloadedOrDownloading=" + isDownloadedOrDownloading );

        mDownloadMenuItem.setVisible( !isDownloadedOrDownloading );
        mDownloadMenuItem.setEnabled( !isDownloadedOrDownloading );
        mDeleteMenuItem.setVisible( isDownloadedOrDownloading );
        mDeleteMenuItem.setEnabled( isDownloadedOrDownloading );

    }

    private boolean isDownloading() {

        if( null == mEpisodeInfoHolder ) {
            return false;
        }

        boolean isDownloading = false;
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(
                DownloadManager.STATUS_PAUSED|
                        DownloadManager.STATUS_PENDING|
                        DownloadManager.STATUS_RUNNING|
                        DownloadManager.STATUS_SUCCESSFUL);
        Cursor cur = mDownloadManager.query(query);
        int col = cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
        for(cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            isDownloading = isDownloading || (mEpisodeInfoHolder.getEpisodeFilename() == cur.getString(col));
        }
        cur.close();

        return isDownloading;
    }

    private void queueDownload() {
        Log.i( TAG, "queueDownload : enter" );

        if( null == mEpisodeInfoHolder ) {
            return;
        }

        if( !isDownloading() ) {

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mEpisodeInfoHolder.getEpisodeFileUrl()));

            // only download via Any Newtwork Connection
            // TODO: Setup preferences to allow user to decide if Mobile or WIFI networks should be used for downloads
            //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            request.setTitle(mEpisodeInfoHolder.getShowPrefix() + ":" + mEpisodeInfoHolder.getEpisodeNumber());
            request.setDescription(mEpisodeInfoHolder.getEpisodeTitle());

            // show download status in notification bar
            request.setVisibleInDownloadsUi(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(this, null, mEpisodeInfoHolder.getEpisodeFilename());
            request.setMimeType( null );

            // enqueue this request
            long downloadId = mDownloadManager.enqueue(request);

            if (downloadId > 0) {

                mEpisodeInfoHolder.setEpisodeDownloadId(downloadId);
                mEpisodeInfoHolder.setEpisodeDownloaded( true );

                swapMenuItems();

                ContentValues values = new ContentValues();
                values.put(EpisodeConstants.FIELD_DOWNLOAD_ID, downloadId);
                values.put(EpisodeConstants.FIELD_DOWNLOADED, 0);
                getContentResolver().update(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, mEpisodeId), values, null, null);
            }

        }

    }

    private void deleteEpisode() {
        Log.i( TAG, "deleteEpisode : enter" );

        if( null == mEpisodeInfoHolder ) {
            return;
        }

        mDownloadManager.remove( mEpisodeInfoHolder.getEpisodeDownloadId() );

        File externalFile = new File(getExternalFilesDir(null), mEpisodeInfoHolder.getEpisodeFilename());
        Log.i( TAG, "deleteEpisode : externalFile=" + externalFile.getAbsolutePath() );
        if( externalFile.exists() ) {
            boolean deleted = externalFile.delete();
            if( deleted ) {
                Log.i( TAG, "deleteEpisode : externalFile deleted!" );

            }
        }

        mEpisodeInfoHolder.setEpisodeDownloadId( -1 );
        mEpisodeInfoHolder.setEpisodeDownloaded( false );

        swapMenuItems();

        ContentValues values = new ContentValues();
        values.put(EpisodeConstants.FIELD_DOWNLOAD_ID, -1);
        values.put(EpisodeConstants.FIELD_DOWNLOADED, 0);
        getContentResolver().update(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, mEpisodeId), values, null, null);

    }

    private void updateSeekBarPosition( int currentPosition ) {
        // TODO: update seek bar position
    }

    private class PlaybackBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Playback Broadcast - " + intent.getAction());
            if( intent.getAction().equals( AudioPlayerService.EVENT_STATUS ) ) {
                int currentPosition = intent.getIntExtra( AudioPlayerService.EXTRA_CURRENT_POSITION, -1 );
                updateSeekBarPosition( currentPosition );

                boolean isPlaying = intent.getBooleanExtra( AudioPlayerService.EXTRA_IS_PLAYING, false );
                if( isPlaying ) {
                    mPlayButton.setVisibility( View.GONE );
                    mPauseButton.setVisibility( View.VISIBLE );
                    mPauseButton.setEnabled(true);
                    mBackButton.setEnabled(true);
                    mSkipButton.setEnabled(true);
                }
            }
        }
    };
}
