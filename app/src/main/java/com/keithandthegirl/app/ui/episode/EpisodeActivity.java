package com.keithandthegirl.app.ui.episode;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.services.media.MediaService;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.gallery.EpisodeImageGalleryFragment;
import com.keithandthegirl.app.ui.gallery.ImageGalleryInfoHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 *
 * TODO remember scroll location when coming back from gallery
 */
public class EpisodeActivity extends AbstractBaseActivity implements EpisodeFragment.EpisodeEventListener {

    private static final String TAG = EpisodeActivity.class.getSimpleName();

    public static final String ARG_EPISODE_KEY = "ARG_EPISODE_KEY";

    private long mEpisodeId;
    private EpisodeInfoHolder mEpisodeInfoHolder;

    private CollapsingToolbarLayout mCollapsingToolbar;
    private TextView episodeDate, episodeNumber;
    private ImageView episodeHeaderBackgroundImageView;


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_episode;
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById( R.id.collapsing_toolbar );
        episodeHeaderBackgroundImageView = (ImageView) findViewById( R.id.episodeHeaderBackgroundImageView );
        episodeDate = (TextView) findViewById( R.id.episode_date );
        episodeNumber = (TextView) findViewById( R.id.episode_number );

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

        episodeDate.setText( mEpisodeInfoHolder.getEpisodePosted() );
        episodeNumber.setText( "Episode: " + mEpisodeInfoHolder.getEpisodeNumber() );
        Picasso.with( this ).load( mEpisodeInfoHolder.getShowCoverImageUrl()).into( episodeHeaderBackgroundImageView );

        mCollapsingToolbar.setTitle(mEpisodeInfoHolder.getEpisodeTitle() );

        if( mEpisodeInfoHolder.isEpisodePublic() ) {
            Log.d(TAG, "public episode");
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