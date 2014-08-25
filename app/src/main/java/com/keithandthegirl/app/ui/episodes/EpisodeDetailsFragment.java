package com.keithandthegirl.app.ui.episodes;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.DetailConstants;
import com.keithandthegirl.app.db.model.EndpointConstants;
import com.keithandthegirl.app.db.model.WorkItemConstants;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Created by dmfrey on 4/29/14.
 */
public class EpisodeDetailsFragment extends Fragment {
    private static final String TAG = EpisodeDetailsFragment.class.getSimpleName();

    private long mEpisodeId;
    private int mEpisodeNumber;
    private String mShowName;

    private WebView mNotesWebView;

    private DetailsObserver detailsObserver = new DetailsObserver();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param args Arguments.
     * @return A new instance of fragment EpisodeDetailsFragment.
     */
    public static EpisodeDetailsFragment newInstance( Bundle args ) {

        EpisodeDetailsFragment fragment = new EpisodeDetailsFragment();
        fragment.setArguments( args );

        return fragment;
    }

    public EpisodeDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        setRetainInstance( true );

        if( null != getArguments()) {
            mEpisodeId = getArguments().getLong( EpisodeActivity.EPISODE_KEY );
            mEpisodeNumber = getArguments().getInt( EpisodeActivity.EPISODE_NUMBER_KEY );
            mShowName = getArguments().getString( EpisodeActivity.SHOW_PREFIX_KEY );
        }

        getActivity().getContentResolver().registerContentObserver( DetailConstants.CONTENT_URI, true, detailsObserver );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        return inflater.inflate( R.layout.fragment_episode_details, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );

        mNotesWebView = (WebView) getActivity().findViewById( R.id.episode_details_notes );

        updateView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getActivity().getContentResolver().unregisterContentObserver( detailsObserver );
    }

    private void updateView() {
//        boolean scheduleDownload = false;

        Cursor cursor = getActivity().getContentResolver().query( DetailConstants.CONTENT_URI, null, DetailConstants.FIELD_SHOWID + " = ? AND NOT (" + DetailConstants.FIELD_NOTES + " IS NULL OR " + DetailConstants.FIELD_NOTES + " = ?)", new String[] { String.valueOf( mEpisodeId ), "" }, null );
        if( cursor.moveToNext() ) {
            Log.v( TAG, "updateView : display details" );

            String notes = cursor.getString( cursor.getColumnIndex( DetailConstants.FIELD_NOTES ) );
            notes = "<ul><li>" + notes.replaceAll( "\r\n", "</li><li>" ) + "</li></ul>";

            mNotesWebView.loadData( notes, "text/html", "utf-8" );
            mNotesWebView.setBackgroundColor( Color.TRANSPARENT );
        } else {
            Log.v( TAG, "updateView : schedule detail download" );

//            scheduleDownload = true;
        }
        cursor.close();

//        if( scheduleDownload ) {

            scheduleWorkItem();

//        }
    }

    private void scheduleWorkItem() {
        ContentValues values = new ContentValues();
        values.put( WorkItemConstants.FIELD_NAME, mShowName + " " + mEpisodeId + " details" );
        values.put( WorkItemConstants.FIELD_FREQUENCY, WorkItemConstants.Frequency.ON_DEMAND.name() );
        values.put( WorkItemConstants.FIELD_DOWNLOAD, WorkItemConstants.Download.JSON.name() );
        values.put( WorkItemConstants.FIELD_ENDPOINT, EndpointConstants.Type.DETAILS.name() );
        values.put( WorkItemConstants.FIELD_ADDRESS, EndpointConstants.DETAILS );
        values.put( WorkItemConstants.FIELD_PARAMETERS, "?showid=" + mEpisodeId );
        values.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.NEVER.name() );
        values.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

        Cursor cursor = getActivity().getContentResolver().query( WorkItemConstants.CONTENT_URI, null, WorkItemConstants.FIELD_ADDRESS + " = ? AND " + WorkItemConstants.FIELD_PARAMETERS + " = ?", new String[] { EndpointConstants.DETAILS, "?showid=" + mEpisodeId }, null );
        if( cursor.moveToNext() ) {
            Log.v( TAG, "scheduleWorkItem : update already scheduled" );

        } else {
            Log.v( TAG, "scheduleWorkItem : scheduling update" );

            getActivity().getContentResolver().insert( WorkItemConstants.CONTENT_URI, values );

        }
        cursor.close();
    }

    private class DetailsObserver extends ContentObserver {

        public DetailsObserver() {
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
