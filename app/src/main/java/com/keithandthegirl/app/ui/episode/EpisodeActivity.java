package com.keithandthegirl.app.ui.episode;

import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.services.media.AudioPlayerService;
import com.keithandthegirl.app.services.media.MediaService;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.gallery.EpisodeImageGalleryFragment;
import com.keithandthegirl.app.ui.gallery.ImageGalleryInfoHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EpisodeActivity extends AbstractBaseActivity implements EpisodeFragment.EpisodeEventListener,
                                                                     OnClickListener, OnSeekBarChangeListener {
    public static final String EPISODE_KEY = "EPISODE_KEY";
    private static final String TAG = EpisodeActivity.class.getSimpleName();
    private long mEpisodeId;
    private LinearLayout mPlayerControls;
    private Button mInitializeButton, mPlayButton, mPauseButton, mStopButton, mBackButton, mSkipButton;
    private SeekBar mSeekBar;

    private EpisodeInfoHolder mEpisodeInfoHolder;

//    private PlaybackBroadcastReceiver mPlaybackBroadcastReceiver = new PlaybackBroadcastReceiver();
    private MediaServiceBroadcastReceiver mMediaServiceBroadcastReceiver = new MediaServiceBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);

        Bundle extras = getIntent().getExtras();
        if (extras.containsKey(EPISODE_KEY)) {
            mEpisodeId = extras.getLong(EPISODE_KEY);
        }

        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, EpisodeFragment.newInstance(mEpisodeId))
                    .commit();
        }

        final View transportLayout = findViewById(R.id.playbackLayout);
        ImageView imageView = (ImageView) findViewById(R.id.thumb);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                View container = findViewById(R.id.container);
                int transportViewHeight = (int) getResources().getDimension(R.dimen.transport_view_height);
                transportLayout.animate().translationY(transportViewHeight);
                ValueAnimator.ofInt(container.getHeight(), container.getHeight() + transportViewHeight );

            }
        });

        mPlayerControls = (LinearLayout) findViewById( R.id.playbackLayout);
        mInitializeButton = (Button) findViewById(R.id.initialize);
        mInitializeButton.setEnabled(true);
        mInitializeButton.setOnClickListener(this);

        mPlayButton = (Button) findViewById(R.id.play);
        mPlayButton.setEnabled(true);
        mPlayButton.setOnClickListener(this);

        mPauseButton = (Button) findViewById(R.id.pause);
        mPauseButton.setEnabled(true);
        mPauseButton.setOnClickListener(this);

        mStopButton = (Button) findViewById(R.id.stop);
        mStopButton.setEnabled(true);
        mStopButton.setOnClickListener(this);

        mBackButton = (Button) findViewById(R.id.back);
        mBackButton.setEnabled(true);
        mBackButton.setOnClickListener(this);

        mSkipButton = (Button) findViewById(R.id.skip);
        mSkipButton.setEnabled(true);
        mSkipButton.setOnClickListener(this);

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener( this );
        mSeekBar.setEnabled( true );
    }

    @Override
    protected void onResume() {
        super.onResume();

//        IntentFilter playbackBroadcastIntentFilter = new IntentFilter(AudioPlayerService.EVENT_STATUS);
//        registerReceiver(mPlaybackBroadcastReceiver, playbackBroadcastIntentFilter);
//
//        Intent intent = new Intent(this, AudioPlayerService.class);
//        intent.setAction(AudioPlayerService.ACTION_IS_PLAYING);
//        startService(intent);

        IntentFilter mediaServiceBroadcastIntentFilter = new IntentFilter(MediaService.EVENT_STATUS);
        registerReceiver(mMediaServiceBroadcastReceiver, mediaServiceBroadcastIntentFilter);

        Intent intent = new Intent(this, MediaService.class);
        intent.setAction(MediaService.ACTION_STATUS);
        startService(intent);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.episode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0 ) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if( null != mPlaybackBroadcastReceiver ) {
//            unregisterReceiver(mPlaybackBroadcastReceiver);
//        }

        if( null != mMediaServiceBroadcastReceiver ) {
            unregisterReceiver(mMediaServiceBroadcastReceiver);
        }

    }

    @Override
    public void onEpisodeLoaded(final EpisodeInfoHolder episodeInfoHolder) {
        mEpisodeInfoHolder = episodeInfoHolder;
        assert getActionBar() != null;
        getActionBar().setTitle(episodeInfoHolder.getShowName());
        getActionBar().setTitle( mEpisodeInfoHolder.getShowName() );

        if( mEpisodeInfoHolder.isEpisodePublic() ) {
            mPlayerControls.setVisibility(View.VISIBLE);
            mPlayButton.setEnabled(true);

            mSeekBar.setMax(mEpisodeInfoHolder.getEpisodeLength() * 1000);
            mSeekBar.setProgress(mEpisodeInfoHolder.getEpisodeLastPlayed());
            onProgressChanged( mSeekBar, mEpisodeInfoHolder.getEpisodeLastPlayed(), false );
        } else {
            mPlayerControls.setVisibility( View.GONE );
        }
        // TODO Enable UI better now that we have episodeId
        // TODO also need to save it for config change

    }

    @Override
    public void onShowImageClicked(final int position, final List<String> imageUrls) {

        ArrayList<ImageGalleryInfoHolder> infoHolderList = new ArrayList<ImageGalleryInfoHolder>();
        for (String string : imageUrls) {
            infoHolderList.add(new ImageGalleryInfoHolder(string, ""));
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, EpisodeImageGalleryFragment.newInstance(position, infoHolderList))
                .addToBackStack(EpisodeImageGalleryFragment.STACK_NAME)
                .commit();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;

        switch( v.getId() ) {

            case R.id.initialize :
                intent = new Intent(this, MediaService.class);
                intent.setAction(MediaService.ACTION_URL);
                if( mEpisodeInfoHolder.isEpisodeDownloaded() ) {
                    File externalFile = new File(getExternalFilesDir(null), mEpisodeInfoHolder.getEpisodeFilename());
                    intent.setData(Uri.fromFile(externalFile));
                } else {
                    intent.setData( Uri.parse( mEpisodeInfoHolder.getEpisodeFileUrl() ) );
                }
                intent.putExtra(MediaService.EXTRA_EPISODE_ID, mEpisodeId);

                break;

            case R.id.play :
//                intent = new Intent(this, AudioPlayerService.class);
//                intent.setAction(AudioPlayerService.ACTION_PLAY);
//                intent.putExtra(AudioPlayerService.EXTRA_EPISODE_ID, mEpisodeId);

                intent = new Intent(this, MediaService.class);
                intent.setAction(MediaService.ACTION_PLAY);

                mPlayButton.setVisibility( View.GONE );
                mPauseButton.setVisibility( View.VISIBLE );
                break;

            case R.id.pause :
//                intent = new Intent(this, AudioPlayerService.class);
//                intent.setAction(AudioPlayerService.ACTION_PAUSE);

                intent = new Intent(this, MediaService.class);
                intent.setAction(MediaService.ACTION_PAUSE);

                mPlayButton.setVisibility( View.VISIBLE );
                mPauseButton.setVisibility( View.GONE );
                break;

            case R.id.back :
//                intent = new Intent(this, AudioPlayerService.class);
//                intent.setAction(AudioPlayerService.ACTION_REW);
                intent = new Intent(this, MediaService.class);
                intent.setAction(MediaService.ACTION_REWIND);
                break;

            case R.id.skip :
//                intent = new Intent(this, AudioPlayerService.class);
//                intent.setAction(AudioPlayerService.ACTION_FF);
                intent = new Intent(this, MediaService.class);
                intent.setAction(MediaService.ACTION_FASTFORWARD);
                break;
        }

        if(intent != null) {
//            intent.putExtra(AudioPlayerService.EXTRA_EPISODE_ID, mEpisodeId);
            startService(intent);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.i(TAG, "onProgressChanged : progress=" + progress + " of " + mSeekBar.getMax());

        mSeekBar.setProgress( progress );

        if( fromUser ) {
            Intent intent = new Intent(this, MediaService.class);
            intent.setAction(MediaService.ACTION_SEEK);
            intent.putExtra(MediaService.EXTRA_SEEK_POSITION, progress);
            startService(intent);

        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void updateSeekBarPosition( int currentPosition ) {
        onProgressChanged( mSeekBar, currentPosition, false );
    }

//    private class PlaybackBroadcastReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if( intent.getAction().equals( AudioPlayerService.EVENT_STATUS ) ) {
//                int currentPosition = intent.getIntExtra( AudioPlayerService.EXTRA_CURRENT_POSITION, -1 );
//                updateSeekBarPosition( currentPosition );
//
//                boolean isPlaying = intent.getBooleanExtra( AudioPlayerService.EXTRA_IS_PLAYING, false );
//                if( isPlaying ) {
//                    mPlayButton.setVisibility( View.GONE );
//                    mPauseButton.setVisibility( View.VISIBLE );
//                    mPauseButton.setEnabled(true);
//                    mBackButton.setEnabled(true);
//                    mSkipButton.setEnabled(true);
//                }
//            }
//        }
//    }

    private class MediaServiceBroadcastReceiver extends BroadcastReceiver {

        private final String TAG = MediaServiceBroadcastReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.v( TAG, "onReceive : enter" );

            if( intent.getAction().equals( MediaService.EVENT_STATUS ) ) {

                MediaService.State state = MediaService.State.valueOf(intent.getStringExtra(MediaService.EXTRA_STATE));
                if( state.equals( MediaService.State.Playing ) || state.equals( MediaService.State.Paused ) ) {
//                    Log.v( TAG, "onReceive : MediaService is playing or paused" );

                    int currentPosition = intent.getIntExtra( MediaService.EXTRA_CURRENT_POSITION, -1 );
                    updateSeekBarPosition( currentPosition );

                    mInitializeButton.setVisibility( View.GONE );
                    mInitializeButton.setEnabled( false );

                }

                if( state.equals( MediaService.State.Playing ) ) {
//                    Log.v( TAG, "onReceive : MediaService is playing" );

                    mPlayButton.setVisibility( View.GONE );
                    mPauseButton.setVisibility( View.VISIBLE );

                    mBackButton.setEnabled( true );
                    mSkipButton.setEnabled( true );
                    mSeekBar.setEnabled( true );

                }

                if( state.equals( MediaService.State.Paused ) ) {
//                    Log.v( TAG, "onReceive : MediaService is paused" );

                    mPlayButton.setVisibility( View.VISIBLE );
                    mPauseButton.setVisibility( View.GONE );

                    mBackButton.setEnabled( false );
                    mSkipButton.setEnabled( false );
                    mSeekBar.setEnabled( false );

                }

                if( !state.equals( MediaService.State.Playing ) && !state.equals( MediaService.State.Paused ) ) {
//                    Log.v( TAG, "onReceive : MediaService is not playing or paused" );

                    mInitializeButton.setVisibility( View.VISIBLE );

                    mPlayButton.setVisibility( View.GONE );
                    mPauseButton.setVisibility( View.GONE );

                    mBackButton.setEnabled( false );
                    mSkipButton.setEnabled( false );
                    mSeekBar.setEnabled( false );

                }

            }

//            Log.v( TAG, "onReceive : exit" );
        }

    }

}
