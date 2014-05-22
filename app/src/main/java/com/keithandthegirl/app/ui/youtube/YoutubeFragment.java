package com.keithandthegirl.app.ui.youtube;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.DatabaseHelper;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.db.model.Youtube;
import com.keithandthegirl.app.ui.VideoPlayerActivity;
import com.keithandthegirl.app.ui.YoutubeFragmentActivity;
import com.keithandthegirl.app.utils.ImageCache;
import com.keithandthegirl.app.utils.ImageFetcher;

import org.joda.time.DateTime;

/**
 * Created by dmfrey on 4/17/14.
 */
public class YoutubeFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = YoutubeFragment.class.getSimpleName();

    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;

    DatabaseHelper dbHelper;
    Cursor cursor;
    YoutubeCursorAdapter mAdapter;

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        Log.v(TAG, "onCreateLoader : enter");

        String[] projection = null;

        String selection = null;

        String[] selectionArgs = null;

        CursorLoader cursorLoader = new CursorLoader( getActivity(), Youtube.CONTENT_URI, projection, selection, selectionArgs, Youtube.FIELD_YOUTUBE_PUBLISHED + " DESC" );

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

    public YoutubeFragment() { }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        mImageThumbSize = getResources().getDimensionPixelSize( R.dimen.list_image_thumbnail_size );
        mImageThumbSpacing = getResources().getDimensionPixelSize( R.dimen.image_thumbnail_spacing );

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams( getActivity(), IMAGE_CACHE_DIR );

        cacheParams.setMemCacheSizePercent( 0.25f ); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher( getActivity(), mImageThumbSize );
//        mImageFetcher.setLoadingImage( R.drawable.empty_photo );
        mImageFetcher.addImageCache( getActivity().getSupportFragmentManager(), cacheParams );

    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );

        setRetainInstance( true );

        getLoaderManager().initLoader( 0, getArguments(), this );
        mAdapter = new YoutubeCursorAdapter( getActivity() );
        setListAdapter( mAdapter );

        getListView().setFastScrollEnabled( true );

    }

    @Override
    public void onResume() {
        Log.v( TAG, "onResume : enter" );

        super.onResume();
        mImageFetcher.setExitTasksEarly( false );
        mAdapter.notifyDataSetChanged();

        Log.v( TAG, "onResume : exit" );
    }

    @Override
    public void onPause() {
        Log.v( TAG, "onPause : enter" );

        super.onPause();
        mImageFetcher.setPauseWork( false );
        mImageFetcher.setExitTasksEarly( true );
        mImageFetcher.flushCache();

        Log.v( TAG, "onPause : exit" );
    }

    @Override
    public void onDestroy() {
        Log.v( TAG, "onDestroy : enter" );

        super.onDestroy();
        mImageFetcher.closeCache();

        Log.v( TAG, "onDestroy : exit" );
    }

    @Override
    public void onListItemClick( ListView l, View v, int position, long id ) {
        Log.v( TAG, "onListItemClick : enter" );

        Cursor c = ( (YoutubeCursorAdapter) l.getAdapter() ).getCursor();
        c.moveToPosition( position );

        String youtubeId = c.getString( c.getColumnIndex( Youtube.FIELD_YOUTUBE_ID ) );
        c.close();

        Intent intent = new Intent( getActivity(), YoutubeFragmentActivity.class );
        intent.putExtra( YoutubeFragmentActivity.YOUTUBE_VIDEO_KEY, youtubeId );
        startActivity( intent );

        Log.v( TAG, "onListItemClick : exit" );
    }


    private class YoutubeCursorAdapter extends CursorAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        public YoutubeCursorAdapter( Context context ) {
            super( context, null, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {

            View view = mInflater.inflate( R.layout.youtube_item_row, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.thumbnail = (ImageView) view.findViewById( R.id.youtube_thumbnail );
            refHolder.title = (TextView) view.findViewById( R.id.youtube_title );
            refHolder.select = (ImageView) view.findViewById( R.id.youtube_select );

            view.setTag( refHolder );

            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {

            ViewHolder mHolder = (ViewHolder) view.getTag();

            mHolder.title.setText( cursor.getString( cursor.getColumnIndex( Youtube.FIELD_YOUTUBE_TITLE ) ) );

            String thumbnail = cursor.getString( cursor.getColumnIndex( Youtube.FIELD_YOUTUBE_THUMBNAIL ) );
            if( null != thumbnail && !"".equals( thumbnail ) ) {

                mHolder.thumbnail.setVisibility( View.VISIBLE );

                mImageFetcher.loadImage( thumbnail, mHolder.thumbnail );

            } else {

                mHolder.thumbnail.setVisibility( View.GONE );

            }


        }

    }

    private static class ViewHolder {

        ImageView thumbnail;
        TextView title;
        ImageView select;

        ViewHolder() { }

    }

}
