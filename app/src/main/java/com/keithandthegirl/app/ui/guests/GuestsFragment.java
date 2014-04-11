package com.keithandthegirl.app.ui.guests;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.DatabaseHelper;
import com.keithandthegirl.app.db.model.Guest;
import com.keithandthegirl.app.utils.ImageCache;
import com.keithandthegirl.app.utils.ImageFetcher;

public class GuestsFragment extends ListFragment {

    private static final String TAG = GuestsFragment.class.getSimpleName();

    private static final String RAW_GUESTS_QUERY =
            "SELECT distinct " +
            "    g._id, g.realname, g.pictureurl, count( eg.showguestid) as count " +
            "FROM " +
            "    guest g left join episode_guests eg on g._id = eg.showguestid " +
            "group by " +
            "    eg.showguestid " +
            "order by " +
            "    eg.showid desc";

    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;

    DatabaseHelper dbHelper;
    Cursor cursor;
    GuestCursorAdapter mAdapter;

    public GuestsFragment() { }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.v( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        mImageThumbSize = getResources().getDimensionPixelSize( R.dimen.list_image_thumbnail_size );
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
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated( savedInstanceState );

        setRetainInstance( true );

        dbHelper = new DatabaseHelper( getActivity() );
        cursor = dbHelper.getReadableDatabase().rawQuery( RAW_GUESTS_QUERY, null );

        mAdapter = new GuestCursorAdapter( getActivity(), cursor );
        setListAdapter( mAdapter );

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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

    }

    private class GuestCursorAdapter extends CursorAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        public GuestCursorAdapter( Context context, Cursor cursor ) {
            super( context, cursor, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {

            View view = mInflater.inflate( R.layout.guest_row, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.image = (ImageView) view.findViewById( R.id.guest_image );
            refHolder.realName = (TextView) view.findViewById( R.id.guest_real_name );
            refHolder.episodes = (TextView) view.findViewById( R.id.guest_episodes );

            view.setTag( refHolder );

            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {

            ViewHolder mHolder = (ViewHolder) view.getTag();

            String pictureUrl = cursor.getString( cursor.getColumnIndex( Guest.FIELD_PICTUREURL ) );
            if( null != pictureUrl && !"".equals( pictureUrl ) ) {

//                Uri pictureUri = Uri.parse(pictureUrl);
//                if( mContext.getFileStreamPath( pictureUri.getLastPathSegment() ).exists() ) {
//
//                    String path = mContext.getFileStreamPath( pictureUri.getLastPathSegment() ).getAbsolutePath();
//                    mHolder.image.setImageBitmap( ImageUtils.decodeSampledBitmapFromFile( path, 75, 75 ) );
//
//                } else {
//
//                    mHolder.image.setImageBitmap( null );
//                    mHolder.image.setVisibility( View.GONE );
//
//                }

                mHolder.image.setVisibility( View.VISIBLE );

                mImageFetcher.loadImage( pictureUrl, mHolder.image );

            } else {

                mHolder.image.setVisibility( View.GONE );

            }

            mHolder.realName.setText( cursor.getString( cursor.getColumnIndex( Guest.FIELD_REALNAME ) ) );
            mHolder.episodes.setText( "Episodes: " + cursor.getString( cursor.getColumnIndex( "count" ) ) );

        }

    }

    private static class ViewHolder {

        ImageView image;
        TextView realName;
        TextView episodes;

        ViewHolder() { }

    }

}
