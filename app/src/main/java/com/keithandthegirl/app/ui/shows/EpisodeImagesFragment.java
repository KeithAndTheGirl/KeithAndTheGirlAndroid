package com.keithandthegirl.app.ui.shows;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Image;
import com.keithandthegirl.app.ui.EpisodeActivity;
import com.keithandthegirl.app.ui.widgets.RecyclingImageView;
import com.keithandthegirl.app.utils.ImageCache;
import com.keithandthegirl.app.utils.ImageFetcher;

/**
 * Created by dmfrey on 4/30/14.
 */
public class EpisodeImagesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = EpisodeImagesFragment.class.getSimpleName();

    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;

    private long mEpisodeId;

    EpisodeImageCursorAdapter mAdapter;

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        Log.v(TAG, "onCreateLoader : enter");

        String[] projection = { Image._ID, Image.FIELD_MEDIAURL };

        String selection = Image.FIELD_SHOWID + " = ?";

        String[] selectionArgs = new String[] { String.valueOf( mEpisodeId ) };

        CursorLoader cursorLoader = new CursorLoader( getActivity(), Image.CONTENT_URI, projection, selection, selectionArgs, Image.FIELD_DISPLAY_ORDER );

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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param args Arguments.
     * @return A new instance of fragment EpisodeDetailsFragment.
     */
    public static EpisodeImagesFragment newInstance( Bundle args ) {

        EpisodeImagesFragment fragment = new EpisodeImagesFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public EpisodeImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.v( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        setRetainInstance( true );

        if( null != getArguments() ) {
            mEpisodeId = getArguments().getLong( EpisodeActivity.EPISODE_KEY );
        }

        mImageThumbSize = getResources().getDimensionPixelSize( R.dimen.image_thumbnail_size );
        mImageThumbSpacing = getResources().getDimensionPixelSize( R.dimen.image_thumbnail_spacing );

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams( getActivity(), IMAGE_CACHE_DIR );

        cacheParams.setMemCacheSizePercent( 0.25f ); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher( getActivity(), mImageThumbSize );
//        mImageFetcher.setLoadingImage( R.drawable.empty_photo );
        mImageFetcher.addImageCache( getActivity().getSupportFragmentManager(), cacheParams );

        Log.v( TAG, "onCreate : exit" );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        Log.v( TAG, "onCreateView : enter" );

        final View rootView = inflater.inflate( R.layout.fragment_episode_images, container, false );

        Log.v( TAG, "onCreateView : exit" );
        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated( savedInstanceState );

        setRetainInstance( true );

        getLoaderManager().initLoader( 0, getArguments(), this );

        mAdapter = new EpisodeImageCursorAdapter( getActivity() );

        final GridView mGridView = (GridView) getActivity().findViewById( R.id.episode_images_gridview );
        mGridView.setAdapter( mAdapter );
//        mGridView.setOnItemClickListener( this );

        Log.v( TAG, "onActivityCreated : exit" );
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

    private class EpisodeImageCursorAdapter extends CursorAdapter {

        private final String TAG = EpisodeImageCursorAdapter.class.getSimpleName();

        private final Context mContext;
        private LayoutInflater mInflater;

        public EpisodeImageCursorAdapter( Context context ) {
            super( context, null, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );

        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {
            Log.v(TAG, "newView : enter");

            View view = mInflater.inflate( R.layout.episode_image_grid_item, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.mediaUrl = (RecyclingImageView) view.findViewById( R.id.episode_grid_item_image );
            refHolder.mediaUrl.setScaleType( ImageView.ScaleType.CENTER_CROP );

            view.setTag( refHolder );

            Log.v( TAG, "newView : exit" );
            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {
            Log.v( TAG, "bindView : enter" );

            ViewHolder mHolder = (ViewHolder) view.getTag();

            String mediaUrl = cursor.getString( cursor.getColumnIndex( Image.FIELD_MEDIAURL ) );
            Log.d( TAG, "bindView : mediaUrl=" + mediaUrl );

            mImageFetcher.loadImage( mediaUrl, mHolder.mediaUrl );

            Log.v( TAG, "bindView : exit" );
        }

    }

    private static class ViewHolder {

        RecyclingImageView mediaUrl;

        ViewHolder() { }

    }

}
