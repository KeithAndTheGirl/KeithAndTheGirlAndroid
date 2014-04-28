package com.keithandthegirl.app.ui.shows;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
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
import com.keithandthegirl.app.db.DatabaseHelper;
import com.keithandthegirl.app.db.model.EpisodeGuests;
import com.keithandthegirl.app.db.model.Guest;
import com.keithandthegirl.app.ui.EpisodeActivity;
import com.keithandthegirl.app.ui.widgets.RecyclingImageView;
import com.keithandthegirl.app.utils.ImageCache;
import com.keithandthegirl.app.utils.ImageFetcher;

/**
 * Created by dmfrey on 4/26/14.
 */
public class EpisodeGuestImagesFragment extends Fragment /* implements LoaderManager.LoaderCallbacks<Cursor> */ {

    private static final String TAG = EpisodeGuestImagesFragment.class.getSimpleName();

    private static final String RAW_GUESTS_QUERY =
            "SELECT " +
            "    g._id, g.pictureurl " +
            "FROM " +
            "    guest g left join episode_guests eg on g._id = eg.showguestid " +
            "WHERE " +
            "    eg.showid = ?";


    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;

    DatabaseHelper dbHelper;
    Cursor cursor;
    EpisodeGuestCursorAdapter mAdapter;

    private long mEpisodeId;

//    @Override
//    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
//        Log.v(TAG, "onCreateLoader : enter");
//
//        String[] projection = null;
//
//        String selection = EpisodeGuests.FIELD_SHOWID + " = ?";
//
//        String[] selectionArgs = new String[] { String.valueOf( mEpisodeId ) };
//
//        CursorLoader cursorLoader = new CursorLoader( getActivity(), Uri.withAppendedPath( Guest.CONTENT_URI, "/Episodes" ), projection, selection, selectionArgs, null );
//
//        Log.v( TAG, "onCreateLoader : exit" );
//        return cursorLoader;
//    }
//
//    @Override
//    public void onLoadFinished( Loader<Cursor> cursorLoader, Cursor cursor ) {
//        Log.v( TAG, "onLoadFinished : enter" );
//
//        mAdapter.swapCursor( cursor );
//
//        Log.v( TAG, "onLoadFinished : exit" );
//    }
//
//    @Override
//    public void onLoaderReset( Loader<Cursor> cursorLoader ) {
//        Log.v( TAG, "onLoaderReset : enter" );
//
//        mAdapter.swapCursor( null );
//
//        Log.v( TAG, "onLoaderReset : exit" );
//    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param args Arguments.
     * @return A new instance of fragment EpisodeHeaderFragment.
     */
    public static EpisodeGuestImagesFragment newInstance( Bundle args ) {

        EpisodeGuestImagesFragment fragment = new EpisodeGuestImagesFragment();
        fragment.setArguments( args );

        return fragment;
    }

    public EpisodeGuestImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.v( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        if( null != getArguments() ) {
            mEpisodeId = getArguments().getLong( EpisodeActivity.EPISODE_KEY );
            Log.v( TAG, "onCreate : mEpisodeId=" + mEpisodeId );
        }

        mImageThumbSize = getResources().getDimensionPixelSize( R.dimen.list_image_thumbnail_size );
        mImageThumbSpacing = getResources().getDimensionPixelSize( R.dimen.image_thumbnail_spacing );

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams( getActivity(), IMAGE_CACHE_DIR );

        cacheParams.setMemCacheSizePercent( 0.25f ); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher( getActivity(), mImageThumbSize );
//        mImageFetcher.setLoadingImage( R.drawable.empty_photo );
        mImageFetcher.addImageCache( getActivity().getSupportFragmentManager(), cacheParams );

        Log.v( TAG, "onCreate : exit") ;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        // Inflate the layout for this fragment

        return inflater.inflate( R.layout.fragment_episode_guest_images, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated( savedInstanceState );

        setRetainInstance( true );

        dbHelper = new DatabaseHelper( getActivity() );
        cursor = dbHelper.getReadableDatabase().rawQuery( RAW_GUESTS_QUERY, new String[] { String.valueOf( mEpisodeId ) } );

        if( null != cursor ) {

            getActivity().startManagingCursor(cursor);
            mAdapter = new EpisodeGuestCursorAdapter( getActivity(), cursor );

            final GridView mGridView = (GridView) getActivity().findViewById( R.id.episode_guest_images_gridview );
            mGridView.setAdapter( mAdapter );

        }

        Log.v( TAG, "onActivityCreated : exit" );
    }

    @Override
    public void onResume() {
        Log.v( TAG, "onResume : enter" );
        super.onResume();

        mImageFetcher.setExitTasksEarly( false );
//        mAdapter.notifyDataSetChanged();

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
        cursor.close();
        dbHelper.close();

        Log.v( TAG, "onDestroy : exit" );
    }

    private class EpisodeGuestCursorAdapter extends CursorAdapter {

        private final String TAG = EpisodeGuestCursorAdapter.class.getSimpleName();

        private final Context mContext;
        private LayoutInflater mInflater;

        public EpisodeGuestCursorAdapter( Context context, Cursor cursor ) {
            super( context, cursor, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );

        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {
            Log.v( TAG, "newView : enter" );

            View view = mInflater.inflate( R.layout.episode_guest_grid_item, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.guestImage = (RecyclingImageView) view.findViewById( R.id.episode_guest_grid_item_image );
            refHolder.guestImage.setScaleType( ImageView.ScaleType.CENTER_CROP );

            view.setTag( refHolder );

            Log.v( TAG, "newView : exit" );
            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {
            Log.v( TAG, "bindView : enter" );

            ViewHolder mHolder = (ViewHolder) view.getTag();

            String coverUrl = cursor.getString( cursor.getColumnIndex( Guest.FIELD_PICTUREURL ) );
            Log.v( TAG, "bindView : coverUrl=" + coverUrl );

            mImageFetcher.loadImage( coverUrl, mHolder.guestImage );

            Log.v( TAG, "bindView : exit" );
        }

    }

    private static class ViewHolder {

        RecyclingImageView guestImage;

        ViewHolder() { }

    }

}
