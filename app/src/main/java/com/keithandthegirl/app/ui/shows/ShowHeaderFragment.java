package com.keithandthegirl.app.ui.shows;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.utils.ImageCache;
import com.keithandthegirl.app.utils.ImageFetcher;

/**
 * Created by dmfrey on 3/30/14.
 */
public class ShowHeaderFragment extends Fragment {

    private static final String TAG = ShowHeaderFragment.class.getSimpleName();

    Context mContext;
    ImageView mCoverImageView;
    TextView mTitleTextView, mDescriptionTextView;

    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.v( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

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

        View rootView = inflater.inflate( R.layout.fragment_show_header, container, false );

        Log.v( TAG, "onCreateView : exit" );
        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated( savedInstanceState );

        mContext = getActivity();

        mCoverImageView = (ImageView) getActivity().findViewById( R.id.show_coverimage );
        mTitleTextView = (TextView) getActivity().findViewById( R.id.show_title );
        mDescriptionTextView = (TextView) getActivity().findViewById( R.id.show_description );
        mDescriptionTextView.setMovementMethod( new ScrollingMovementMethod() );

        if( null != getArguments() ) {

            long showNameId = getArguments().getLong( ShowFragment.SHOW_NAME_ID_KEY );

            updateHeader(showNameId);

        }

        Log.v( TAG, "onActivityCreated : enter" );
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

        Log.v( TAG, "onDestroy : exit" );
    }

    public void updateHeader( long showNameId ) {
        Log.v( TAG, "updateHeader : enter" );

        Log.v( TAG, "updateHeader : showNameId=" + showNameId );

        String[] projection = new String[] { Show._ID, Show.FIELD_NAME, Show.FIELD_DESCRIPTION, Show.FIELD_COVERIMAGEURL };

        Cursor cursor = mContext.getContentResolver().query( ContentUris.withAppendedId( Show.CONTENT_URI, showNameId ), projection, null, null, null );
        if( cursor.moveToNext() ) {

            String coverUrl = cursor.getString( cursor.getColumnIndex( Show.FIELD_COVERIMAGEURL ) );

            mTitleTextView.setText( cursor.getString( cursor.getColumnIndex( Show.FIELD_NAME ) ) );
            mDescriptionTextView.setText( cursor.getString( cursor.getColumnIndex( Show.FIELD_DESCRIPTION ) ) );

            mImageFetcher.loadImage( coverUrl, mCoverImageView );

        }
        cursor.close();

        Log.v( TAG, "updateHeader : exit" );
    }

}
