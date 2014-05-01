package com.keithandthegirl.app.ui.shows;

import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.DatabaseHelper;
import com.keithandthegirl.app.db.model.EpisodeGuests;
import com.keithandthegirl.app.db.model.Guest;
import com.keithandthegirl.app.ui.EpisodeActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmfrey on 4/26/14.
 */
public class EpisodeGuestsFragment extends Fragment {

    private static final String TAG = EpisodeGuestsFragment.class.getSimpleName();

    private static final String RAW_GUESTS_QUERY =
            "SELECT " +
            "    g._id, g.realname " +
            "FROM " +
            "    guest g left join episode_guests eg on g._id = eg.showguestid " +
            "WHERE " +
            "    eg.showid = ?";


    private DatabaseHelper dbHelper;
    Cursor cursor;
    private long mEpisodeId;

    private TextView mNames;

    private GuestsObserver guestsObserver = new GuestsObserver();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param args Arguments.
     * @return A new instance of fragment EpisodeHeaderFragment.
     */
    public static EpisodeGuestsFragment newInstance( Bundle args ) {

        EpisodeGuestsFragment fragment = new EpisodeGuestsFragment();
        fragment.setArguments( args );

        return fragment;
    }

    public EpisodeGuestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.v( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        if( null != getArguments() ) {
            mEpisodeId = getArguments().getLong( EpisodeActivity.EPISODE_KEY );
        }

        getActivity().getContentResolver().registerContentObserver( Guest.CONTENT_URI, true, guestsObserver );

        Log.v( TAG, "onCreate : exit") ;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        // Inflate the layout for this fragment

        return inflater.inflate( R.layout.fragment_episode_guests, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );

        dbHelper = new DatabaseHelper( getActivity() );

        mNames = (TextView) getActivity().findViewById( R.id.episode_guest_names );

        updateView();

    }

    @Override
    public void onDestroy() {
        Log.v( TAG, "onDestroy : enter" );
        super.onDestroy();

        dbHelper.close();

        getActivity().getContentResolver().unregisterContentObserver( guestsObserver );

        Log.v( TAG, "onDestroy : exit" );
    }

    private void updateView() {

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery( RAW_GUESTS_QUERY, new String[] { String.valueOf( mEpisodeId ) } );

        List<Long> guestIds = new ArrayList<Long>();
        List<String> guestNames = new ArrayList<String>();
        while( cursor.moveToNext() ) {

            guestIds.add( cursor.getLong( cursor.getColumnIndex( Guest._ID ) ) );
            guestNames.add( cursor.getString( cursor.getColumnIndex( Guest.FIELD_REALNAME ) ) );

        }
        cursor.close();

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

            mNames.setText( combined );
        }

    }

    private class GuestsObserver extends ContentObserver {

        public GuestsObserver() {
            super( null );

        }

        @Override
        public void onChange( boolean selfChange ) {

            this.onChange( selfChange, null );

        }

        @Override
        public void onChange( boolean selfChange, Uri uri ) {

            getActivity().runOnUiThread( new Runnable() {

                @Override
                public void run() {

                    updateView();

                }

            });

        }

    }

}
