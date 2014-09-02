package com.keithandthegirl.app.ui.episode;

import android.animation.Animator;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.services.media.AudioPlayerService;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.gallery.EpisodeImageGalleryFragment;
import com.keithandthegirl.app.ui.gallery.ImageGalleryInfoHolder;

import java.util.ArrayList;
import java.util.List;

public class EpisodeActivity extends AbstractBaseActivity implements EpisodeFragment.EpisodeEventListener,
                                                                     OnClickListener {
    public static final String EPISODE_KEY = "EPISODE_KEY";
    private static final String TAG = EpisodeActivity.class.getSimpleName();
    private long mEpisodeId;
    private LinearLayout mPlayerControls;
    private Button mPlayButton, mPauseButton, mBackButton, mSkipButton;

    private EpisodeInfoHolder mEpisodeInfoHolder;

    private PlaybackBroadcastReceiver mPlaybackBroadcastReceiver = new PlaybackBroadcastReceiver();

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
                transportLayout.animate().translationY(getResources().getDimension(R.dimen.transport_view_height)).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(final Animator animation) {}

                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        findViewById(R.id.container).requestLayout();
                    }

                    @Override
                    public void onAnimationCancel(final Animator animation) {}

                    @Override
                    public void onAnimationRepeat(final Animator animation) {}
                });

            }
        });

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

        if( null != mPlaybackBroadcastReceiver ) {
            unregisterReceiver(mPlaybackBroadcastReceiver);
        }
    }

    @Override
    public void onEpisodeLoaded(final EpisodeInfoHolder episodeInfoHolder) {
        mEpisodeInfoHolder = episodeInfoHolder;
        assert getActionBar() != null;
        getActionBar().setTitle(episodeInfoHolder.getShowName());
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

    private void updateSeekBarPosition( int currentPosition ) {
        // TODO: update seek bar position
    }

    private class PlaybackBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
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
    }
}
