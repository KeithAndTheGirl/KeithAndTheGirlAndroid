package com.keithandthegirl.app.ui.shows;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.EpisodeGuests;
import com.keithandthegirl.app.db.model.Guest;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.ui.EpisodeActivity;
import com.keithandthegirl.app.ui.widgets.RecyclingImageView;
import com.keithandthegirl.app.utils.ImageCache;
import com.keithandthegirl.app.utils.ImageFetcher;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by dmfrey on 4/26/14.
 */
public class EpisodeHeaderFragment extends Fragment {

    private static final String TAG = EpisodeHeaderFragment.class.getSimpleName();
    private static final DateTimeFormatter mFormatter = DateTimeFormat.forPattern( "MMM d, yyyy" ).withZone( DateTimeZone.forTimeZone( TimeZone.getTimeZone( "America/New_York" ) ) );

    private static final String IMAGE_CACHE_DIR = "episode";

    private int mImageWidth;
    private ImageFetcher mImageFetcher;

    private int mEpisodeNumber;
    private String mEpisodeTitle, mShowPrefix, mShowCoverImageUrl;
    private long mEpisodePosted;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param args Arguments.
     * @return A new instance of fragment EpisodeHeaderFragment.
     */
    public static EpisodeHeaderFragment newInstance( Bundle args ) {

        EpisodeHeaderFragment fragment = new EpisodeHeaderFragment();
        fragment.setArguments( args );

        return fragment;
    }

    public EpisodeHeaderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.v( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        setRetainInstance( true );

        if( null != getArguments() ) {
            mEpisodeNumber = getArguments().getInt( EpisodeActivity.EPISODE_NUMBER_KEY );
            mEpisodeTitle = getArguments().getString( EpisodeActivity.EPISODE_TITLE_KEY );
            mEpisodePosted = getArguments().getLong( EpisodeActivity.EPISODE_POSTED_KEY );
            mShowPrefix = getArguments().getString( EpisodeActivity.SHOW_NAME_KEY );
            mShowCoverImageUrl = getArguments().getString( EpisodeActivity.SHOW_COVER_IMAGE_URL_KEY );
        }

        WindowManager wm = (WindowManager) getActivity().getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();
        if( Build.VERSION.SDK_INT >= 13 ) {
            Point size = new Point();
            display.getSize( size );
            mImageWidth = size.x;
        } else {
            mImageWidth = display.getWidth();
        }

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams( getActivity(), IMAGE_CACHE_DIR );

        cacheParams.setMemCacheSizePercent( 0.25f ); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher( getActivity(), mImageWidth );
        mImageFetcher.addImageCache( getActivity().getSupportFragmentManager(), cacheParams );

        Log.v( TAG, "onCreate : exit" );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        // Inflate the layout for this fragment

        return inflater.inflate( R.layout.fragment_episode_header, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );

        RecyclingImageView header = (RecyclingImageView) getActivity().findViewById( R.id.episode_heading );
        TextView number = (TextView) getActivity().findViewById( R.id.episode_number );
        TextView showDate = (TextView) getActivity().findViewById( R.id.episode_date );
        TextView title = (TextView) getActivity().findViewById( R.id.episode_title );

        showDate.setText( mFormatter.print( mEpisodePosted ) );
        number.setText( mShowPrefix + " " + mEpisodeNumber );
        title.setText( mEpisodeTitle );
        mImageFetcher.loadImage( mShowCoverImageUrl, header );

    }

    @Override
    public void onResume() {
        Log.v( TAG, "onResume : enter" );
        super.onResume();

        mImageFetcher.setExitTasksEarly( false );

        Log.v( TAG, "onResume : exit" );
    }

    @Override
    public void onPause() {
        Log.v( TAG, "onPause : enter" );
        super.onPause();

        mImageFetcher.setPauseWork( false );
        mImageFetcher.setExitTasksEarly( true );
        mImageFetcher.flushCache();

        Log.v( TAG, "onPause : exit" );
    }

    @Override
    public void onDestroy() {
        Log.v( TAG, "onDestroy : enter" );
        super.onDestroy();

        mImageFetcher.closeCache();

        Log.v( TAG, "onDestroy : exit" );
    }

}
