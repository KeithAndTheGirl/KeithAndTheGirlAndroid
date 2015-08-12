package com.keithandthegirl.app.ui.episode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.services.media.MediaService;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.gallery.EpisodeImageGalleryFragment;
import com.keithandthegirl.app.ui.gallery.ImageGalleryInfoHolder;
import com.keithandthegirl.app.ui.player.PlaybackStatusFragment;

import java.util.ArrayList;

import timber.log.Timber;

/**
 *
 * TODO remember scroll location when coming back from gallery
 */
public class EpisodeActivity extends AppCompatActivity implements EpisodeFragment.EpisodeEventListener {
    public static final String ARG_EPISODE_KEY = "ARG_EPISODE_KEY";

    private long mEpisodeId;
    private EpisodeInfoHolder mEpisodeInfoHolder;
    private PlaybackStatusFragment mPlaybackControlsFragment;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView(R.layout.activity_detail);

        Bundle extras = getIntent().getExtras();
        if( extras.containsKey( ARG_EPISODE_KEY ) ) {
            mEpisodeId = extras.getLong( ARG_EPISODE_KEY );
        }

        if( savedInstanceState == null ) {
            getSupportFragmentManager().beginTransaction()
                    .add( R.id.container, EpisodeFragment.newInstance( mEpisodeId ) )
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent( this, MediaService.class );
        intent.setAction( MediaService.ACTION_STATUS );
        startService( intent );
    }

    @Override
    protected void onStart() {
        super.onStart();

        mPlaybackControlsFragment = (PlaybackStatusFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback_controls);
        if (null == mPlaybackControlsFragment) {
            throw new IllegalStateException("Missing fragment with id 'controls'. Cannot continue.");
        }

        hidePlaybackControls();
    }

    private void hidePlaybackControls() {
        getSupportFragmentManager().beginTransaction()
                .hide(mPlaybackControlsFragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch( item.getItemId() ) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if( getSupportFragmentManager().getBackStackEntryCount() > 0 ) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    NavUtils.navigateUpFromSameTask( this );
                }

                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onEpisodeLoaded( final EpisodeInfoHolder episodeInfoHolder ) {
        mEpisodeInfoHolder = episodeInfoHolder;
        setTitle(mEpisodeInfoHolder.getEpisodeTitle());
//        mPlayerFragment.loadEpisodeInfo(episodeInfoHolder);
//        mPlayerFragment.requestVisible(true);

        if( mEpisodeInfoHolder.isEpisodePublic() ) {
            Timber.d("public episode");
        }
        // TODO Enable UI better now that we have episodeId
        // TODO also need to save it for config change
    }

    @Override
    public void onEpisodeImageClicked( final int position, final ArrayList<ImageGalleryInfoHolder> imageGalleryInfoHolderList ) {
        getSupportFragmentManager().beginTransaction()
                .replace( R.id.container, EpisodeImageGalleryFragment.newInstance(position, imageGalleryInfoHolderList ) )
                .addToBackStack( EpisodeImageGalleryFragment.STACK_NAME )
                .commit();
    }
}