package com.keithandthegirl.app.ui;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.Show;

/**
 * Created by dmfrey on 3/21/14.
 */
public class EpisodesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = EpisodesFragment.class.getSimpleName();

    public static final String SHOW_NAME_ID_KEY = "showNameId";

    EpisodeCursorAdapter mAdapter;

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        Log.v(TAG, "onCreateLoader : enter");

        String[] projection = { Episode._ID, Episode.FIELD_TITLE };

        String selection = Episode.FIELD_SHOWNAMEID + "=?";

        String[] selectionArgs = new String[] { String.valueOf( args.getLong( SHOW_NAME_ID_KEY ) ) };

        CursorLoader cursorLoader = new CursorLoader( getActivity(), Episode.CONTENT_URI, projection, selection, selectionArgs, Episode.FIELD_NUMBER + " DESC" );

        Log.v( TAG, "onCreateLoader : exit" );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished( Loader<Cursor> cursorLoader, Cursor cursor ) {
        Log.v( TAG, "onLoadFinished : enter" );

        mAdapter.swapCursor( cursor );

        Log.v( TAG, "onLoadFinished : exit" );
    }

    @Override
    public void onLoaderReset( Loader<Cursor> cursorLoader ) {
        Log.v( TAG, "onLoaderReset : enter" );

        mAdapter.swapCursor( null );

        Log.v( TAG, "onLoaderReset : exit" );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader( 0, getArguments(), this );
        mAdapter = new EpisodeCursorAdapter( getActivity().getApplicationContext() );
        setListAdapter( mAdapter );

        Log.v( TAG, "onActivityCreated : exit" );
    }

    public void updateEpisodesView( long showNameId ) {
        Log.v( TAG, "updateEpisodesView : enter" );

        Bundle args = new Bundle();
        args.putLong( SHOW_NAME_ID_KEY, showNameId );
        getLoaderManager().restartLoader( 0, args, this );

        Log.v( TAG, "updateEpisodesView : exit" );
    }

    private class EpisodeCursorAdapter extends CursorAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        public EpisodeCursorAdapter( Context context ) {
            super( context, null, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {

            View view = mInflater.inflate( android.R.layout.simple_list_item_1, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.name = (TextView) view.findViewById( android.R.id.text1 );

            view.setTag( refHolder );

            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {

            ViewHolder mHolder = (ViewHolder) view.getTag();

            mHolder.name.setText( cursor.getString( cursor.getColumnIndex( Episode.FIELD_TITLE ) ) );
        }

    }

    private static class ViewHolder {

        TextView name;

        ViewHolder() { }

    }

}
