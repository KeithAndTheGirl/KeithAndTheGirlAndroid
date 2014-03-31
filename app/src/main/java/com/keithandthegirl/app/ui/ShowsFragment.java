package com.keithandthegirl.app.ui;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.keithandthegirl.app.db.model.Show;

/**
 * Created by dmfrey on 3/21/14.
 */
public class ShowsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ShowsFragment.class.getSimpleName();

    OnShowSelectedListener mCallback;

    ShowCursorAdapter mAdapter;

    public interface OnShowSelectedListener {
        public void onShowSelected( long showId );
    }

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        Log.v(TAG, "onCreateLoader : enter");

        String[] projection = { Show._ID, Show.FIELD_NAME, Show.FIELD_COVERIMAGEURL };

        String selection = null;

        String[] selectionArgs = null;

        CursorLoader cursorLoader = new CursorLoader( getActivity(), Show.CONTENT_URI, projection, selection, selectionArgs, Show.FIELD_SORTORDER );

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
        Log.v(TAG, "onLoaderReset : enter");

        mAdapter.swapCursor(null);

        Log.v( TAG, "onLoaderReset : exit" );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader( 0, getArguments(), this );
        mAdapter = new ShowCursorAdapter( getActivity().getApplicationContext() );
        setListAdapter( mAdapter );

        Log.v( TAG, "onActivityCreated : exit" );
    }

    @Override
    public void onAttach( Activity activity ) {
        Log.v( TAG, "onAttach : enter" );
        super.onAttach( activity );

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnShowSelectedListener) activity;
        } catch( ClassCastException e ) {
            throw new ClassCastException( activity.toString() + " must implement OnShowSelectedListener");
        }

        Log.v( TAG, "onAttach : exit" );
    }

    @Override
    public void onListItemClick( ListView l, View v, int position, long id ) {

        // Send the event to the host activity
        mCallback.onShowSelected( id );

    }

    private class ShowCursorAdapter extends CursorAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        public ShowCursorAdapter( Context context ) {
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

            mHolder.name.setText( cursor.getString( cursor.getColumnIndex( Show.FIELD_NAME ) ) );
        }

    }

    private static class ViewHolder {

        TextView name;

        ViewHolder() { }

    }

}
