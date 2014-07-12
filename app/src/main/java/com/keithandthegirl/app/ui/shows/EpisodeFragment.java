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
import android.widget.ImageView;
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

import static android.os.Build.*;

public class EpisodeFragment extends Fragment {

    private static final String TAG = EpisodeFragment.class.getSimpleName();
    private static final DateTimeFormatter mFormatter = DateTimeFormat.forPattern( "MMM d, yyyy" ).withZone( DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York")) );

    private static final String IMAGE_CACHE_DIR = "episode";

    private int mImageWidth;
    private ImageFetcher mImageFetcher;

    private long mEpisodeId;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param episodeId Episode ID.
     * @return A new instance of fragment EpisodeFragment.
     */
    public static EpisodeFragment newInstance( long episodeId ) {

        EpisodeFragment fragment = new EpisodeFragment();

        Bundle args = new Bundle();
        args.putLong( EpisodeActivity.EPISODE_KEY, episodeId );
        fragment.setArguments( args );

        return fragment;
    }

    public EpisodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.v( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        if( null != getArguments() ) {
            mEpisodeId = getArguments().getLong( EpisodeActivity.EPISODE_KEY );
        }

        WindowManager wm = (WindowManager) getActivity().getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();
        if( VERSION.SDK_INT >= 13 ) {
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

        return inflater.inflate( R.layout.fragment_episode, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );

        LinearLayout guestView = (LinearLayout) getActivity().findViewById( R.id.episode_guests );
        RecyclingImageView header = (RecyclingImageView) getActivity().findViewById( R.id.episode_heading );
        TextView number = (TextView) getActivity().findViewById( R.id.episode_number );
        TextView showDate = (TextView) getActivity().findViewById( R.id.episode_date );
        TextView title = (TextView) getActivity().findViewById( R.id.episode_title );
        TextView names = (TextView) getActivity().findViewById( R.id.episode_guest_names );
        TextView played = (TextView) getActivity().findViewById( R.id.episode_played );
        TextView downloaded = (TextView) getActivity().findViewById( R.id.episode_downloaded );

        int episodeNumber = -1, showNameId = -1;
        String prefix = "", coverImageUrl = "";

        Cursor cursor = getActivity().getContentResolver().query( ContentUris.withAppendedId( Episode.CONTENT_URI, mEpisodeId ), null, null, null, null );
        if( cursor.moveToNext() ) {

            long instant = cursor.getLong( cursor.getColumnIndex( Episode.FIELD_TIMESTAMP ) );

            episodeNumber = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_NUMBER ) );
            showNameId = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_SHOWNAMEID ) );

            showDate.setText( cursor.getString( cursor.getColumnIndex( Episode.FIELD_POSTED ) ) );
            title.setText( cursor.getString( cursor.getColumnIndex( Episode.FIELD_TITLE ) ) );

        }
        cursor.close();

        cursor = getActivity().getContentResolver().query( ContentUris.withAppendedId( Show.CONTENT_URI, showNameId ), null, null, null, null );
        if( cursor.moveToNext() ) {

            prefix = cursor.getString( cursor.getColumnIndex( Show.FIELD_PREFIX ) );
            coverImageUrl = cursor.getString( cursor.getColumnIndex( Show.FIELD_COVERIMAGEURL ) );

        }
        cursor.close();

        List<Long> guestIds = new ArrayList<Long>();
        cursor = getActivity().getContentResolver().query( EpisodeGuests.CONTENT_URI, null, EpisodeGuests.FIELD_SHOWID + " = ?", new String[] { String.valueOf( mEpisodeId ) }, null );
        while( cursor.moveToNext() ) {

            guestIds.add( cursor.getLong( cursor.getColumnIndex( EpisodeGuests.FIELD_SHOWGUESTID ) ) );

        }
        cursor.close();

        if( !guestIds.isEmpty() ) {

            List<String> guestNames = new ArrayList<String>();
            List<String> guestImages = new ArrayList<String>();
            for( Long guestId : guestIds ) {

                cursor = getActivity().getContentResolver().query( ContentUris.withAppendedId( Guest.CONTENT_URI, guestId ), null, null, null, null );
                if( cursor.moveToNext() ) {

                    guestNames.add( cursor.getString( cursor.getColumnIndex( Guest.FIELD_REALNAME ) ) );
                    guestImages.add( cursor.getString( cursor.getColumnIndex( Guest.FIELD_PICTUREFILENAME ) ) );

                }
                cursor.close();

            }

            if( !guestNames.isEmpty() ) {

                String combined = "";
                int count = 0;
                for( String guestName : guestNames ) {

                    combined += guestName;

                    if( count < guestNames.size() - 1 ) {
                        combined += ", ";
                    }

                    count++;
                }

                names.setText( combined );
            }

        }

        number.setText( prefix + " " + episodeNumber );
        mImageFetcher.loadImage( coverImageUrl, header );

    }

    @Override
    public void onResume() {
        Log.v( TAG, "onResume : enter" );
        super.onResume();

        mImageFetcher.setExitTasksEarly(false);

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
