package com.keithandthegirl.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Show;

/**
 * Created by dmfrey on 3/21/14.
 */
public class ShowsGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ShowsGridFragment.class.getSimpleName();

    OnShowSelectedListener mCallback;

    ShowCursorAdapter mAdapter;

    public interface OnShowSelectedListener {
        public void onShowSelected( long showId );
    }

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        Log.v(TAG, "onCreateLoader : enter");

        String[] projection = { Show._ID, Show.FIELD_NAME, Show.FIELD_PREFIX, Show.FIELD_COVERIMAGEURL, Show.FIELD_VIP };

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
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        Log.v( TAG, "onCreateView : enter" );

        View rootView = inflater.inflate( R.layout.fragment_shows, container, false );

        Log.v( TAG, "onCreateView : exit" );
        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader( 0, getArguments(), this );
        mAdapter = new ShowCursorAdapter( getActivity().getApplicationContext() );

        GridView gridview = (GridView) getActivity().findViewById(R.id.shows_gridview);
        gridview.setAdapter( mAdapter );

        gridview.setOnItemClickListener( new AdapterView.OnItemClickListener() {

            public void onItemClick( AdapterView<?> parent, View v, int position, long id ) {

                mCallback.onShowSelected( id );

            }

        });

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

    private class ShowCursorAdapter extends CursorAdapter {

        private final String TAG = ShowCursorAdapter.class.getSimpleName();

        private Context mContext;
        private LayoutInflater mInflater;

        public ShowCursorAdapter( Context context ) {
            super( context, null, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {

            View view = mInflater.inflate( R.layout.show_grid_item, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.coverImage = (ImageView) view.findViewById( R.id.show_grid_item_coverimage );
            refHolder.vip = (TextView) view.findViewById( R.id.show_grid_item_vip );
            refHolder.name = (TextView) view.findViewById( R.id.show_grid_item_name );

            view.setTag( refHolder );

            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {

            ViewHolder mHolder = (ViewHolder) view.getTag();

            String name = cursor.getString( cursor.getColumnIndex( Show.FIELD_NAME ) );
            String prefix = cursor.getString(cursor.getColumnIndex(Show.FIELD_PREFIX));
            boolean vip = cursor.getLong( cursor.getColumnIndex( Show.FIELD_VIP ) ) == 0 ? false : true;


            String filename = prefix + "_150x150.jpg";
            Log.i( TAG, "bindView : filename=" + filename );

            Bitmap bitmap = BitmapFactory.decodeFile( mContext.getFileStreamPath( filename ).getAbsolutePath() );

            mHolder.coverImage.setImageBitmap( bitmap );
            mHolder.name.setText( name );

            if( vip ) {
                mHolder.vip.setVisibility( View.VISIBLE );
            } else {
                mHolder.vip.setVisibility( View.GONE );
            }

        }

    }

    private static class ViewHolder {

        ImageView coverImage;
        TextView vip;
        TextView name;

        ViewHolder() { }

    }

}
