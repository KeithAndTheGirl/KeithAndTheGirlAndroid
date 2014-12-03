package com.keithandthegirl.app.ui.episode;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.services.media.MediaService;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.gallery.EpisodeImageGalleryFragment;
import com.keithandthegirl.app.ui.gallery.ImageGalleryInfoHolder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * TODO remember scroll location when coming back from gallery
 */
public class EpisodeActivity extends AbstractBaseActivity implements EpisodeFragment.EpisodeEventListener,
                                                                     OnClickListener, OnSeekBarChangeListener {
    public static final String EPISODE_KEY = "EPISODE_KEY";
    private static final String TAG = EpisodeActivity.class.getSimpleName();
    private long mEpisodeId;
    private LinearLayout mPlayerControls;
    private Button mPlayButton, mStopButton, mBackButton, mSkipButton;
    private TextView mPlayerTitle, mPlayedLength, mPlayerDuration;
    private SeekBar mSeekBar;

    private EpisodeInfoHolder mEpisodeInfoHolder;

//    private PlaybackBroadcastReceiver mPlaybackBroadcastReceiver = new PlaybackBroadcastReceiver();
    private MediaServiceBroadcastReceiver mMediaServiceBroadcastReceiver = new MediaServiceBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        Bundle extras = getIntent().getExtras();
        if (extras.containsKey(EPISODE_KEY)) {
            mEpisodeId = extras.getLong(EPISODE_KEY);
        }

        final ActionBar actionBar = getSupportActionBar();
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

        mPlayerTitle = (TextView) findViewById( R.id.player_title );

        mPlayerControls = (LinearLayout) findViewById( R.id.playbackLayout);
        mPlayerControls.setVisibility(View.GONE);

        mPlayButton = (Button) findViewById(R.id.play);
        mPlayButton.setEnabled(true);
        mPlayButton.setOnClickListener(this);

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

        mPlayedLength = (TextView) findViewById( R.id.played_length );
        mPlayerDuration = (TextView) findViewById( R.id.player_duration );
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

//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.episode, menu);
//        return true;
//    }
//
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
//        assert getActionBar() != null;
//        getActionBar().setTitle(episodeInfoHolder.getShowName());
//        getActionBar().setTitle( mEpisodeInfoHolder.getShowName() );

        if( mEpisodeInfoHolder.isEpisodePublic() ) {
//            mPlayerControls.setVisibility(View.VISIBLE);

            mSeekBar.setMax(mEpisodeInfoHolder.getEpisodeLength() * 1000);
            mSeekBar.setProgress(mEpisodeInfoHolder.getEpisodeLastPlayed());
            onProgressChanged( mSeekBar, mEpisodeInfoHolder.getEpisodeLastPlayed(), false );
        } else {
//            mPlayerControls.setVisibility( View.GONE );
        }
        // TODO Enable UI better now that we have episodeId
        // TODO also need to save it for config change

    }

    @Override
    public void onShowImageClicked(final int position, final ArrayList<ImageGalleryInfoHolder> imageGalleryInfoHolderList) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, EpisodeImageGalleryFragment.newInstance(position, imageGalleryInfoHolderList))
                .addToBackStack(EpisodeImageGalleryFragment.STACK_NAME)
                .commit();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, MediaService.class);;

        switch( v.getId() ) {

            case R.id.play :
//                intent = new Intent(this, AudioPlayerService.class);
//                intent.setAction(AudioPlayerService.ACTION_PLAY);
//                intent.putExtra(AudioPlayerService.EXTRA_EPISODE_ID, mEpisodeId);

                intent.setAction(MediaService.ACTION_TOGGLE_PLAYBACK);

                break;

            case R.id.stop :

                intent.setAction(MediaService.ACTION_STOP);

                break;

            case R.id.back :
//                intent = new Intent(this, AudioPlayerService.class);
//                intent.setAction(AudioPlayerService.ACTION_REW);
                intent.setAction(MediaService.ACTION_REWIND);

                break;

            case R.id.skip :
//                intent = new Intent(this, AudioPlayerService.class);
//                intent.setAction(AudioPlayerService.ACTION_FF);
                intent.setAction(MediaService.ACTION_FASTFORWARD);

                break;
        }

//            intent.putExtra(AudioPlayerService.EXTRA_EPISODE_ID, mEpisodeId);
        startService(intent);
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

    private class MediaServiceBroadcastReceiver extends BroadcastReceiver {

        private final String TAG = MediaServiceBroadcastReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.v( TAG, "onReceive : enter" );

            if( intent.getAction().equals( MediaService.EVENT_STATUS ) ) {

                long episodeId = intent.getLongExtra( MediaService.EXTRA_EPISODE_ID, -1 );
                if( episodeId > 0 ) {

                    mPlayerTitle.setText(intent.getStringExtra(MediaService.EXTRA_TITLE));
                    mPlayedLength.setText(intent.getStringExtra(MediaService.EXTRA_PLAYED_POSITION));
                    mPlayerDuration.setText(intent.getStringExtra(MediaService.EXTRA_DURATION));
                }

                MediaService.State state = MediaService.State.valueOf(intent.getStringExtra(MediaService.EXTRA_STATE));
                if( state.equals( MediaService.State.Playing ) || state.equals( MediaService.State.Paused ) ) {
//                    Log.v( TAG, "onReceive : MediaService is playing or paused" );

                    int currentPosition = intent.getIntExtra( MediaService.EXTRA_CURRENT_POSITION, -1 );
                    updateSeekBarPosition( currentPosition );

                    mPlayerControls.setVisibility( View.VISIBLE );

                }

                if( state.equals( MediaService.State.Playing ) ) {
//                    Log.v( TAG, "onReceive : MediaService is playing" );

                    mPlayButton.setText( "Pause" );

                    mBackButton.setEnabled( true );
                    mSkipButton.setEnabled( true );
                    mSeekBar.setEnabled( true );

                }

                if( state.equals( MediaService.State.Paused ) ) {
//                    Log.v( TAG, "onReceive : MediaService is paused" );

                    mPlayButton.setText( "Play" );

                    mBackButton.setEnabled( false );
                    mSkipButton.setEnabled( false );
                    mSeekBar.setEnabled( false );

                }

                if( !state.equals( MediaService.State.Playing ) && !state.equals( MediaService.State.Paused ) ) {
//                    Log.v( TAG, "onReceive : MediaService is not playing or paused" );

                    mPlayerControls.setVisibility( View.GONE );

                }

            }

//            Log.v( TAG, "onReceive : exit" );
        }

    }

}
