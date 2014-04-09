package com.keithandthegirl.app.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.keithandthegirl.app.db.model.Guest;
import com.keithandthegirl.app.utils.ImageUtils;

public class GuestsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = GuestsFragment.class.getSimpleName();

    GuestCursorAdapter mAdapter;

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        Log.v(TAG, "onCreateLoader : enter");

        String[] projection = null;

        String selection = null;

        String[] selectionArgs = null;

        CursorLoader cursorLoader = new CursorLoader( getActivity(), Guest.CONTENT_URI, projection, selection, selectionArgs, null );

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

    public GuestsFragment() { }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onCreate( savedInstanceState );

        setRetainInstance( true );

        getLoaderManager().initLoader( 0, getArguments(), this );
        mAdapter = new GuestCursorAdapter( getActivity().getApplicationContext() );
        setListAdapter( mAdapter );

        Log.v( TAG, "onActivityCreated : exit" );
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

    }

    private class GuestCursorAdapter extends CursorAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        public GuestCursorAdapter( Context context ) {
            super( context, null, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {

            View view = mInflater.inflate( R.layout.guest_row, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.image = (ImageView) view.findViewById( R.id.guest_image );
            refHolder.realName = (TextView) view.findViewById( R.id.guest_real_name );

            view.setTag( refHolder );

            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {

            ViewHolder mHolder = (ViewHolder) view.getTag();

            String pictureUrl = cursor.getString( cursor.getColumnIndex( Guest.FIELD_PICTUREURL ) );
            if( null != pictureUrl && !"".equals( pictureUrl ) ) {

                Uri pictureUri = Uri.parse( pictureUrl );
                if( mContext.getFileStreamPath( pictureUri.getLastPathSegment() ).exists() ) {

                    String path = mContext.getFileStreamPath( pictureUri.getLastPathSegment() ).getAbsolutePath();
                    mHolder.image.setImageBitmap( ImageUtils.decodeSampledBitmapFromFile( path, 75, 75 ) );
                    mHolder.image.setVisibility( View.VISIBLE );

                } else {

                    mHolder.image.setImageBitmap( null );
                    mHolder.image.setVisibility( View.GONE );

                }

            }

            mHolder.realName.setText( cursor.getString( cursor.getColumnIndex( Guest.FIELD_REALNAME ) ) );

        }

    }

    private static class ViewHolder {

        ImageView image;
        TextView realName;

        ViewHolder() { }

    }

}
