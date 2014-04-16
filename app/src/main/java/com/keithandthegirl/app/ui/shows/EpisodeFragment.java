package com.keithandthegirl.app.ui.shows;

import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.ui.EpisodeActivity;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.TimeZone;

public class EpisodeFragment extends Fragment {

    private static final DateTimeFormatter mFormatter = DateTimeFormat.forPattern("MMM d, yyyy").withZone( DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York")) );

    private long mEpisodeId;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param episodeId Episode ID.
     * @return A new instance of fragment EpisodeFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        super.onCreate( savedInstanceState );

        if( null != getArguments() ) {
            mEpisodeId = getArguments().getLong( EpisodeActivity.EPISODE_KEY );
        }

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        // Inflate the layout for this fragment

        return inflater.inflate( R.layout.fragment_episode, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );

        Cursor cursor = getActivity().getContentResolver().query( ContentUris.withAppendedId( Episode.CONTENT_URI, mEpisodeId ), null, null, null, null );
        if( cursor.moveToNext() ) {

            TextView number = (TextView) getActivity().findViewById(R.id.episode_number);
            TextView showDate = (TextView) getActivity().findViewById(R.id.episode_date);
            TextView title = (TextView) getActivity().findViewById(R.id.episode_title);
            TextView played = (TextView) getActivity().findViewById(R.id.episode_played);
            TextView downloaded = (TextView) getActivity().findViewById(R.id.episode_downloaded);

            long instant = cursor.getLong( cursor.getColumnIndex( Episode.FIELD_TIMESTAMP ) );

            number.setText( "EPISODE " + cursor.getInt( cursor.getColumnIndex( Episode.FIELD_NUMBER ) ) );
            showDate.setText( mFormatter.print( instant ) );
            title.setText( cursor.getString( cursor.getColumnIndex( Episode.FIELD_TITLE ) ) );

        }
        cursor.close();

    }
}
