package com.keithandthegirl.app.ui.episodes;

import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.EpisodeGuestConstants;
import com.keithandthegirl.app.db.model.GuestConstants;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class EpisodeFragment extends Fragment {
    private static final String TAG = EpisodeFragment.class.getSimpleName();

    private static final DateTimeFormatter mFormatter = DateTimeFormat.forPattern( "MMM d, yyyy" ).withZone( DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York")) );
    private long mEpisodeId;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param episodeId EpisodeConstants ID.
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
        super.onCreate( savedInstanceState );

        if( null != getArguments() ) {
            mEpisodeId = getArguments().getLong(EpisodeActivity.EPISODE_KEY);
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

        LinearLayout guestView = (LinearLayout) getActivity().findViewById( R.id.episode_guests );
        ImageView header = (ImageView) getActivity().findViewById( R.id.episode_heading );
        TextView number = (TextView) getActivity().findViewById( R.id.episode_number );
        TextView showDate = (TextView) getActivity().findViewById( R.id.episode_date );
        TextView title = (TextView) getActivity().findViewById( R.id.episode_title );
        TextView names = (TextView) getActivity().findViewById( R.id.episode_guest_names );
        TextView played = (TextView) getActivity().findViewById( R.id.episode_played );
        TextView downloaded = (TextView) getActivity().findViewById( R.id.episode_downloaded );

        int episodeNumber = -1, showNameId = -1;
        String prefix = "", coverImageUrl = "";

        Cursor cursor = getActivity().getContentResolver().query( ContentUris.withAppendedId( EpisodeConstants.CONTENT_URI, mEpisodeId ), null, null, null, null );
        if( cursor.moveToNext() ) {

            long instant = cursor.getLong( cursor.getColumnIndex( EpisodeConstants.FIELD_TIMESTAMP ) );

            episodeNumber = cursor.getInt( cursor.getColumnIndex( EpisodeConstants.FIELD_NUMBER ) );
            showNameId = cursor.getInt( cursor.getColumnIndex( EpisodeConstants.FIELD_SHOWNAMEID ) );

            showDate.setText( cursor.getString( cursor.getColumnIndex( EpisodeConstants.FIELD_POSTED ) ) );
            title.setText( cursor.getString( cursor.getColumnIndex( EpisodeConstants.FIELD_TITLE ) ) );

        }
        cursor.close();

        cursor = getActivity().getContentResolver().query( ContentUris.withAppendedId( ShowConstants.CONTENT_URI, showNameId ), null, null, null, null );
        if( cursor.moveToNext() ) {

            prefix = cursor.getString( cursor.getColumnIndex( ShowConstants.FIELD_PREFIX ) );
            coverImageUrl = cursor.getString( cursor.getColumnIndex( ShowConstants.FIELD_COVERIMAGEURL ) );

        }
        cursor.close();

        List<Long> guestIds = new ArrayList<Long>();
        cursor = getActivity().getContentResolver().query( EpisodeGuestConstants.CONTENT_URI, null, EpisodeGuestConstants.FIELD_SHOWID + " = ?", new String[] { String.valueOf( mEpisodeId ) }, null );
        while( cursor.moveToNext() ) {

            guestIds.add( cursor.getLong( cursor.getColumnIndex( EpisodeGuestConstants.FIELD_SHOWGUESTID ) ) );

        }
        cursor.close();

        if( !guestIds.isEmpty() ) {

            List<String> guestNames = new ArrayList<String>();
            List<String> guestImages = new ArrayList<String>();
            for( Long guestId : guestIds ) {

                cursor = getActivity().getContentResolver().query( ContentUris.withAppendedId( GuestConstants.CONTENT_URI, guestId ), null, null, null, null );
                if( cursor.moveToNext() ) {

                    guestNames.add( cursor.getString( cursor.getColumnIndex( GuestConstants.FIELD_REALNAME ) ) );
                    guestImages.add( cursor.getString( cursor.getColumnIndex( GuestConstants.FIELD_PICTUREFILENAME ) ) );

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
        Picasso.with(getActivity()).load(coverImageUrl).fit().centerCrop().into(header);

    }
}
