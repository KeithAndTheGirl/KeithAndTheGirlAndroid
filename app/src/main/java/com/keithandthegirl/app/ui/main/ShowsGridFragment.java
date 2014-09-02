package com.keithandthegirl.app.ui.main;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.ui.shows.ShowsActivity;
import com.squareup.picasso.Picasso;

/**
 * Created by dmfrey on 3/21/14.
 */
public class ShowsGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private static final String TAG = ShowsGridFragment.class.getSimpleName();

    ShowCursorAdapter mAdapter;

    public static ShowsGridFragment newInstance() {
        ShowsGridFragment fragment = new ShowsGridFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        final View rootView = inflater.inflate( R.layout.fragment_shows, container, false );
        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );

        getLoaderManager().initLoader( 0, getArguments(), this );

        mAdapter = new ShowCursorAdapter( getActivity() );

        final GridView gridView = (GridView) getActivity().findViewById( R.id.shows_gridview );
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick( AdapterView<?> parent, View v, int position, long id ) {
        final Intent i = new Intent( getActivity(), ShowsActivity.class );
        i.putExtra( ShowsActivity.SHOW_NAME_POSITION_KEY, position );
//        if( Utils.hasJellyBean() ) {
//            // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
//            // show plus the thumbnail image in GridView is cropped. so using
//            // makeScaleUpAnimation() instead.
//            // TODO This options wasn't being used... I disabled it Jeff
////            ActivityOptions options = ActivityOptions.makeScaleUpAnimation( v, 0, 0, v.getWidth(), v.getHeight() );
//            startActivity( i );
//        } else {
            startActivity( i );
//        }
    }

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        String[] projection = { ShowConstants._ID, ShowConstants.FIELD_NAME, ShowConstants.FIELD_PREFIX, ShowConstants.FIELD_COVERIMAGEURL_200, ShowConstants.FIELD_VIP };
        String selection = null;
        String[] selectionArgs = null;

        CursorLoader cursorLoader = new CursorLoader( getActivity(), ShowConstants.CONTENT_URI, projection, selection, selectionArgs, ShowConstants.FIELD_SORTORDER );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished( Loader<Cursor> cursorLoader, Cursor cursor ) {
        mAdapter.swapCursor( cursor );
    }

    @Override
    public void onLoaderReset( Loader<Cursor> cursorLoader ) {
        mAdapter.swapCursor( null );
    }


    private class ShowCursorAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        public ShowCursorAdapter( Context context ) {
            super( context, null, false );
            mInflater = LayoutInflater.from( context );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {
            View view = mInflater.inflate( R.layout.show_grid_item, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.coverImage = (ImageView) view.findViewById( R.id.show_grid_item_coverimage );
            refHolder.coverImage.setScaleType( ImageView.ScaleType.CENTER_CROP );

            refHolder.vip = (TextView) view.findViewById( R.id.show_grid_item_vip );
            refHolder.name = (TextView) view.findViewById( R.id.show_grid_item_name );

            view.setTag( refHolder );
            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {
            ViewHolder mHolder = (ViewHolder) view.getTag();

            String name = cursor.getString( cursor.getColumnIndex( ShowConstants.FIELD_NAME ) );
            String prefix = cursor.getString( cursor.getColumnIndex( ShowConstants.FIELD_PREFIX ) );
            String coverUrl = cursor.getString( cursor.getColumnIndex( ShowConstants.FIELD_COVERIMAGEURL_200 ) );
            boolean vip = cursor.getLong( cursor.getColumnIndex( ShowConstants.FIELD_VIP ) ) == 0 ? false : true;

            mHolder.name.setText( name );

            if( vip ) {
                mHolder.vip.setVisibility( View.VISIBLE );
            } else {
                mHolder.vip.setVisibility( View.GONE );
            }

            Picasso.with(getActivity()).load(coverUrl).fit().centerCrop().into(mHolder.coverImage);
        }
    }

    private static class ViewHolder {
        ImageView coverImage;
        TextView vip;
        TextView name;

        ViewHolder() { }
    }
}

