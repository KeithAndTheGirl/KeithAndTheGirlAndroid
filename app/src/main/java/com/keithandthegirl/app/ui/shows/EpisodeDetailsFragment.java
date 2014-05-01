package com.keithandthegirl.app.ui.shows;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Detail;
import com.keithandthegirl.app.db.model.Endpoint;
import com.keithandthegirl.app.db.model.WorkItem;
import com.keithandthegirl.app.ui.EpisodeActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.w3c.dom.Text;

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
        Log.v( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        if( null != getArguments()) {
            mEpisodeId = getArguments().getLong( EpisodeActivity.EPISODE_KEY );
            mEpisodeNumber = getArguments().getInt( EpisodeActivity.EPISODE_NUMBER_KEY );
            mShowName = getArguments().getString( EpisodeActivity.SHOW_PREFIX_KEY );
        }

        getActivity().getContentResolver().registerContentObserver( Detail.CONTENT_URI, true, detailsObserver );

        Log.v( TAG, "onCreate : exit" );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        // Inflate the layout for this fragment

        return inflater.inflate( R.layout.fragment_episode_details, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated( savedInstanceState );

        mNotesWebView = (WebView) getActivity().findViewById( R.id.episode_details_notes );

        updateView();

        Log.v( TAG, "onActivityCreated : exit" );
    }

    @Override
    public void onDestroyView() {
        Log.v( TAG, "onDestroyView : enter" );
        super.onDestroyView();

        getActivity().getContentResolver().unregisterContentObserver( detailsObserver );

        Log.v(TAG, "onDestroyView : exit");
    }

    private void updateView() {
        Log.v( TAG, "updateView : enter" );

        boolean scheduleDownload = false;

        Cursor cursor = getActivity().getContentResolver().query( Detail.CONTENT_URI, null, Detail.FIELD_SHOWID + " = ? AND NOT (" + Detail.FIELD_NOTES + " IS NULL OR " + Detail.FIELD_NOTES + " = ?)", new String[] { String.valueOf( mEpisodeId ), "" }, null );
        if( cursor.moveToNext() ) {
            Log.v( TAG, "updateView : display details" );

            String notes = cursor.getString( cursor.getColumnIndex( Detail.FIELD_NOTES ) );
            notes = "<ul><li>" + notes.replaceAll( "\r\n", "</li><li>" ) + "</li></ul>";

            mNotesWebView.loadData( notes, "text/html", "utf-8" );
            mNotesWebView.setBackgroundColor( Color.TRANSPARENT );
        } else {
            Log.v( TAG, "updateView : schedule detail download" );

            scheduleDownload = true;
        }
        cursor.close();

        if( scheduleDownload ) {

            scheduleWorkItem();

        }

        Log.v( TAG, "updateView : exit" );
    }

    private void scheduleWorkItem() {
        Log.v( TAG, "scheduleWorkItem : enter" );

        ContentValues values = new ContentValues();
        values.put( WorkItem.FIELD_NAME, mShowName + " " + mEpisodeId + " details" );
        values.put( WorkItem.FIELD_FREQUENCY, WorkItem.Frequency.ON_DEMAND.name() );
        values.put( WorkItem.FIELD_DOWNLOAD, WorkItem.Download.JSON.name() );
        values.put( WorkItem.FIELD_ENDPOINT, Endpoint.Type.DETAILS.name() );
        values.put( WorkItem.FIELD_ADDRESS, Endpoint.DETAILS );
        values.put( WorkItem.FIELD_PARAMETERS, "?showid=" + mEpisodeId );
        values.put( WorkItem.FIELD_STATUS, WorkItem.Status.NEVER.name() );
        values.put( WorkItem.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

        Cursor cursor = getActivity().getContentResolver().query( WorkItem.CONTENT_URI, null, WorkItem.FIELD_ADDRESS + " = ? AND " + WorkItem.FIELD_PARAMETERS + " = ?", new String[] { Endpoint.DETAILS, "?showid=" + mEpisodeId }, null );
        if( cursor.moveToNext() ) {
            Log.v( TAG, "scheduleWorkItem : update already scheduled" );

        } else {
            Log.v( TAG, "scheduleWorkItem : scheduling update" );

            getActivity().getContentResolver().insert( WorkItem.CONTENT_URI, values );

        }
        cursor.close();

        Log.v( TAG, "scheduleWorkItem : exit" );
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
