package com.keithandthegirl.app.ui.shows;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.ui.EpisodeActivity;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.TimeZone;

/**
 * Created by dmfrey on 3/30/14.
 */
public class ShowFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ShowFragment.class.getSimpleName();

    public static final String SHOW_NAME_ID_KEY = "showNameId";

    EpisodeCursorAdapter mAdapter;
    long mShowNameId;

    /**
     * Returns a new instance of this fragment for the given show id.
     */
    public static ShowFragment newInstance( long showNameId ) {

        ShowFragment fragment = new ShowFragment();

        Bundle args = new Bundle();
        args.putLong( SHOW_NAME_ID_KEY, showNameId );
        fragment.setArguments( args );

        return fragment;
    }

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        Log.v(TAG, "onCreateLoader : enter");

        String[] projection = null;

        String selection = Episode.FIELD_SHOWNAMEID + "=?";

        mShowNameId = args.getLong( SHOW_NAME_ID_KEY );
        String[] selectionArgs = new String[] { String.valueOf( mShowNameId ) };

        CursorLoader cursorLoader = new CursorLoader( getActivity(), Episode.CONTENT_URI, projection, selection, selectionArgs, Episode.FIELD_NUMBER + " DESC" );

        Log.v( TAG, "onCreateLoader : exit" );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished( Loader<Cursor> cursorLoader, Cursor cursor ) {
        Log.v( TAG, "onLoadFinished : enter" );

        mAdapter.swapCursor( cursor );
        getListView().setFastScrollEnabled( true );

        Log.v( TAG, "onLoadFinished : exit" );
    }

    @Override
    public void onLoaderReset( Loader<Cursor> cursorLoader ) {
        Log.v( TAG, "onLoaderReset : enter" );

        mAdapter.swapCursor( null );

        Log.v( TAG, "onLoaderReset : exit" );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        Log.v( TAG, "onCreateView : enter" );

        View rootView = inflater.inflate( R.layout.fragment_show, container, false );

        Log.v( TAG, "onCreateView : exit" );
        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated( savedInstanceState );

        if( null != getArguments() ) {

            long showNameId = getArguments().getLong( SHOW_NAME_ID_KEY );

            updateShow( showNameId );

            mAdapter = new EpisodeCursorAdapter( getActivity().getApplicationContext() );
            setListAdapter( mAdapter );

        }

        Log.v( TAG, "onActivityCreated : exit" );
    }

    @Override
    public void onListItemClick( ListView l, View v, int position, long id ) {
        Log.v( TAG, "onListItemClick : enter" );
        super.onListItemClick(l, v, position, id);

        Log.v( TAG, "onListItemClick : id=" + id );

        Intent i = new Intent( getActivity(), EpisodeActivity.class );
        i.putExtra( EpisodeActivity.EPISODE_KEY, id );

        startActivity( i );

        Log.v( TAG, "onListItemClick : exit" );
    }

    public void updateShow( long showNameId ) {
        Log.v( TAG, "updateShow : enter" );

        Log.v( TAG, "updateShow : showNameId=" + showNameId );

        Bundle args = new Bundle();
        args.putLong( SHOW_NAME_ID_KEY, showNameId );
        getLoaderManager().restartLoader( 0, args, this );

        ShowHeaderFragment showHeaderFragment = (ShowHeaderFragment) getChildFragmentManager().findFragmentById( R.id.show_header );
        if( null != showHeaderFragment ) {
            Log.v( TAG, "updateShow : updating show header" );

            showHeaderFragment.updateHeader( showNameId );

        } else {
            Log.v( TAG, "updateShow : adding show header" );

            // Create fragment and give it an argument for the selected article
            ShowHeaderFragment newFragment = new ShowHeaderFragment();
            newFragment.setArguments( args );

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace( R.id.show_header, newFragment );
            //transaction.addToBackStack( null );

            // Commit the transaction
            transaction.commit();
        }

        Log.v( TAG, "updateShow : exit" );
    }

    private class EpisodeCursorAdapter extends CursorAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        String mEpisodesLabel;
        DateTimeFormatter mFormatter = DateTimeFormat.forPattern( "MMM d, yyyy" ).withZone( DateTimeZone.forTimeZone( TimeZone.getTimeZone( "America/New_York" ) ) );

        public EpisodeCursorAdapter( Context context ) {
            super( context, null, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );

            mEpisodesLabel = mContext.getResources().getString( R.string.episode_label );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {

            View view = mInflater.inflate( R.layout.episode_row, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.number = (TextView) view.findViewById( R.id.episode_number );
            refHolder.showDate = (TextView) view.findViewById( R.id.episode_date );
            refHolder.title = (TextView) view.findViewById( R.id.episode_title );
            refHolder.details = (ImageView) view.findViewById( R.id.episode_details );
            refHolder.played = (TextView) view.findViewById( R.id.episode_played );
            refHolder.downloaded = (TextView) view.findViewById( R.id.episode_downloaded );

            view.setTag( refHolder );

            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {

            ViewHolder mHolder = (ViewHolder) view.getTag();

            long id = cursor.getLong( cursor.getColumnIndex( Episode._ID ) );
            long instant = cursor.getLong( cursor.getColumnIndex( Episode.FIELD_TIMESTAMP ) );

            mHolder.number.setText( mEpisodesLabel + " " + cursor.getInt( cursor.getColumnIndex( Episode.FIELD_NUMBER ) ) );
            mHolder.showDate.setText( mFormatter.print( instant ) );
            mHolder.title.setText( cursor.getString( cursor.getColumnIndex( Episode.FIELD_TITLE ) ) );

        }

    }

    private static class ViewHolder {

        TextView number;
        TextView showDate;
        TextView title;
        ImageView details;
        TextView played;
        TextView downloaded;

        ViewHolder() { }

    }

}
