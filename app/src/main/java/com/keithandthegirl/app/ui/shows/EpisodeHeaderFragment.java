package com.keithandthegirl.app.ui.shows;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.ui.EpisodeActivity;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.TimeZone;

/**
 * Created by dmfrey on 4/26/14.
 */
public class EpisodeHeaderFragment extends Fragment {

    private static final String TAG = EpisodeHeaderFragment.class.getSimpleName();
    private static final DateTimeFormatter mFormatter = DateTimeFormat.forPattern( "MMM d, yyyy" ).withZone( DateTimeZone.forTimeZone( TimeZone.getTimeZone( "America/New_York" ) ) );

    private int mEpisodeNumber;
    private String mEpisodeTitle, mShowPrefix, mShowCoverImageUrl, mEpisodePosted;

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
            mEpisodePosted = getArguments().getString( EpisodeActivity.EPISODE_POSTED_KEY );
            mShowPrefix = getArguments().getString( EpisodeActivity.SHOW_NAME_KEY );
            mShowCoverImageUrl = getArguments().getString(EpisodeActivity.SHOW_COVER_IMAGE_URL_KEY);
        }

        Log.v( TAG, "onCreate : exit" );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        // Inflate the layout for this fragment

        return inflater.inflate( R.layout.fragment_episode_header, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);

        ImageView header = (ImageView) getActivity().findViewById( R.id.episode_heading );
        TextView number = (TextView) getActivity().findViewById( R.id.episode_number );
        TextView showDate = (TextView) getActivity().findViewById( R.id.episode_date );
        TextView title = (TextView) getActivity().findViewById( R.id.episode_title );

        showDate.setText( mEpisodePosted );
        number.setText( mShowPrefix + " " + mEpisodeNumber );
        title.setText( mEpisodeTitle );
        Picasso.with(getActivity()).load(mShowCoverImageUrl).fit().centerCrop().into(header);
    }
}
