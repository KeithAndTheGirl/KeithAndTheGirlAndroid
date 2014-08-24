package com.keithandthegirl.app.ui.episodesimpler;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.episodesimpler.gallery.EpisodeImageGalleryFragment;

import java.util.List;
import java.util.Observable;

public class EpisodeActivity extends AbstractBaseActivity implements EpisodeFragment.EpisodeEventListener {
    public static final String EPISODE_KEY = "EPISODE_KEY";
    private static final String TAG = EpisodeActivity.class.getSimpleName();
    private long mEpisodeId;
    private Button mPlayButton, mPauseButton, mBackButton, mSkipButton;
    private String mEpisodeFileUrl;

    private ExoPlayer player;
    private int mCurrentPosition = 0;

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

        mPlayButton = (Button) findViewById(R.id.play);
        mPlayButton.setEnabled(false);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                FrameworkSampleSource sampleSource = new FrameworkSampleSource( EpisodeActivity.this, Uri.parse( mEpisodeFileUrl ), null, 1 );

                player = ExoPlayer.Factory.newInstance(1);
                MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, null, true);

                player.prepare(audioRenderer);
                player.seekTo(mCurrentPosition);
                player.setPlayWhenReady(true);

                mPlayButton.setVisibility( View.GONE );
                mPauseButton.setVisibility( View.VISIBLE );
                mPauseButton.setEnabled(true);
                mBackButton.setEnabled(true);
                mSkipButton.setEnabled(true);

                updatePlaybackPosition( mCurrentPosition );
            }
        });

        mPauseButton = (Button) findViewById(R.id.pause);
        mPauseButton.setEnabled(false);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                player.setPlayWhenReady( false );
                mCurrentPosition = player.getCurrentPosition();

                mPlayButton.setVisibility( View.VISIBLE );
                mPauseButton.setVisibility( View.GONE );
                mPauseButton.setEnabled(false);
                mBackButton.setEnabled(false);
                mSkipButton.setEnabled(false);

                updatePlaybackPosition( mCurrentPosition );

            }
        });

        mBackButton = (Button) findViewById(R.id.back);
        mBackButton.setEnabled(false);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                player.setPlayWhenReady( false );
                mCurrentPosition = player.getCurrentPosition() - 30000;
                player.seekTo( mCurrentPosition );
                player.setPlayWhenReady( true );

                updatePlaybackPosition( mCurrentPosition );

            }
        });

        mSkipButton = (Button) findViewById(R.id.skip);
        mSkipButton.setEnabled(false);
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                player.setPlayWhenReady( false );
                mCurrentPosition = player.getCurrentPosition() + 30000;
                player.seekTo( mCurrentPosition );
                player.setPlayWhenReady( true );

                updatePlaybackPosition( mCurrentPosition );

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        if( null != player ) {
            player.setPlayWhenReady( false );
            mCurrentPosition = player.getCurrentPosition();

            updatePlaybackPosition( mCurrentPosition );

            player.stop();
            player.release();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.episode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEpisodeLoaded(final String episodeFileUrl, final int lastPlayed) {
        mEpisodeFileUrl = episodeFileUrl;
        mCurrentPosition = lastPlayed;
        mPlayButton.setEnabled(true);
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

    private void updatePlaybackPosition( int playbackMs ) {

        ContentValues values = new ContentValues();
        values.put(EpisodeConstants.FIELD_PLAYED, 1 );
        values.put(EpisodeConstants.FIELD_LASTPLAYED, playbackMs );

        getContentResolver().update(ContentUris.withAppendedId( EpisodeConstants.CONTENT_URI, mEpisodeId ), values, null, null );

    }
}
