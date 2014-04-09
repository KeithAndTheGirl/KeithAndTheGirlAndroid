package com.keithandthegirl.app.ui;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.keithandthegirl.app.BuildConfig;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.utils.ImageCache;
import com.keithandthegirl.app.utils.ImageFetcher;
import com.keithandthegirl.app.utils.Utils;

/**
 * Created by dmfrey on 3/21/14.
 */
public class ShowsGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private static final String TAG = ShowsGridFragment.class.getSimpleName();

    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;

    ShowCursorAdapter mAdapter;

    FileObserver fileObserver = null;

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
        Log.v( TAG, "onLoaderReset : enter" );

        mAdapter.swapCursor( null );

        Log.v( TAG, "onLoaderReset : exit" );
    }

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

        final View rootView = inflater.inflate( R.layout.fragment_shows, container, false );

        Log.v( TAG, "onCreateView : exit" );
        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated( savedInstanceState );

        setRetainInstance( true );

        getLoaderManager().initLoader( 0, getArguments(), this );

        mAdapter = new ShowCursorAdapter( getActivity() );

        final GridView mGridView = (GridView) getActivity().findViewById( R.id.shows_gridview );
        mGridView.setAdapter( mAdapter );
        mGridView.setOnItemClickListener( this );
//        mGridView.setOnScrollListener( new AbsListView.OnScrollListener() {
//
//            @Override
//            public void onScrollStateChanged( AbsListView absListView, int scrollState ) {
//
//                // Pause fetcher to ensure smoother scrolling when flinging
//                if( scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING ) {
//
//                    // Before Honeycomb pause image loading on scroll to help with performance
//                    if( !Utils.hasHoneycomb() ) {
//                        mImageFetcher.setPauseWork( true );
//                    }
//
//                } else {
//
//                    mImageFetcher.setPauseWork( false );
//
//                }
//
//            }
//
//            @Override
//            public void onScroll( AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount ) { }
//
//        });

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
//        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
//                new ViewTreeObserver.OnGlobalLayoutListener() {
//
//                    @TargetApi( Build.VERSION_CODES.JELLY_BEAN )
//                    @Override
//                    public void onGlobalLayout() {
//
//                        if( mAdapter.getNumColumns() == 0 ) {
//
//                            final int numColumns = (int) Math.floor( mGridView.getWidth() / ( mImageThumbSize + mImageThumbSpacing ) );
//                            if( numColumns > 0 ) {
//
//                                final int columnWidth = ( mGridView.getWidth() / numColumns ) - mImageThumbSpacing;
//                                mAdapter.setNumColumns( numColumns );
//                                mAdapter.setItemHeight( columnWidth );
//
//                                if( BuildConfig.DEBUG ) {
//                                    Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
//                                }
//
//                                if( Utils.hasJellyBean() ) {
//                                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                                } else {
//                                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                                }
//
//                            }
//
//                        }
//
//                    }
//
//                });

//        fileObserver = new FileObserver( getActivity().getApplicationContext().getFilesDir().getAbsolutePath() ) {
//
//            @Override
//            public void onEvent( int event, String filename ) {
//
//                if( event == FileObserver.CREATE || event == FileObserver.MODIFY ) {
//
//                    if( filename.endsWith( "_cover.jpg" ) ) {
//
//                        getLoaderManager().restartLoader( 0, new Bundle(), ShowsGridFragment.this );
//
//                    }
//                }
//
//            }
//
//        };

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

    @TargetApi( Build.VERSION_CODES.JELLY_BEAN )
    @Override
    public void onItemClick( AdapterView<?> parent, View v, int position, long id ) {
        Log.v( TAG, "onItemClick : enter - position=" + position + ", id=" + id );

        final Intent i = new Intent( getActivity(), ShowsActivity.class );
        i.putExtra( ShowsActivity.SHOW_NAME_POSITION_KEY, position );
        if( Utils.hasJellyBean() ) {
            // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
            // show plus the thumbnail image in GridView is cropped. so using
            // makeScaleUpAnimation() instead.
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation( v, 0, 0, v.getWidth(), v.getHeight() );
            startActivity( i );
        } else {
            startActivity( i );
        }

        Log.v( TAG, "onItemClick : exit" );
    }

    private class ShowCursorAdapter extends CursorAdapter {

        private final String TAG = ShowCursorAdapter.class.getSimpleName();

        private final Context mContext;
        private LayoutInflater mInflater;

//        private int mItemHeight = 0;
//        private int mNumColumns = 0;
//        private int mActionBarHeight = 0;
        //private GridView.LayoutParams mImageViewLayoutParams;

        public ShowCursorAdapter( Context context ) {
            super( context, null, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );

            //mImageViewLayoutParams = new GridView.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );

            // Calculate ActionBar height
//            TypedValue tv = new TypedValue();
//            if( context.getTheme().resolveAttribute( android.R.attr.actionBarSize, tv, true ) ) {
//
//                mActionBarHeight = TypedValue.complexToDimensionPixelSize( tv.data, context.getResources().getDisplayMetrics() );
//
//            }

        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {
            Log.v( TAG, "newView : enter" );

            View view = mInflater.inflate( R.layout.show_grid_item, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.coverImage = (RecyclingImageView) view.findViewById( R.id.show_grid_item_coverimage );
            refHolder.coverImage.setScaleType( ImageView.ScaleType.CENTER_CROP );
            //refHolder.coverImage.setLayoutParams( mImageViewLayoutParams );

            refHolder.vip = (TextView) view.findViewById( R.id.show_grid_item_vip );
            refHolder.name = (TextView) view.findViewById( R.id.show_grid_item_name );

            view.setTag( refHolder );

            Log.v( TAG, "newView : exit" );
            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {
            Log.v( TAG, "bindView : enter" );

            ViewHolder mHolder = (ViewHolder) view.getTag();

            String name = cursor.getString( cursor.getColumnIndex( Show.FIELD_NAME ) );
            String prefix = cursor.getString( cursor.getColumnIndex( Show.FIELD_PREFIX ) );
            String coverUrl = cursor.getString( cursor.getColumnIndex( Show.FIELD_COVERIMAGEURL ) );
            boolean vip = cursor.getLong( cursor.getColumnIndex( Show.FIELD_VIP ) ) == 0 ? false : true;

//            String filename = prefix + "_cover.jpg";
//            String path = mContext.getFileStreamPath( filename ).getAbsolutePath();
//            Log.v( TAG, "bindView : filename=" + filename );

//            mHolder.coverImage.setImageBitmap( ImageUtils.decodeSampledBitmapFromFile( path, 150, 150 ) );

//            if( mHolder.coverImage.getLayoutParams().height != mItemHeight ) {
//                Log.v( TAG, "bindView : height != mItemHeight" );
//
//                mHolder.coverImage.setLayoutParams( mImageViewLayoutParams );
//            }

            mHolder.name.setText( name );

            if( vip ) {
                mHolder.vip.setVisibility( View.VISIBLE );
            } else {
                mHolder.vip.setVisibility( View.GONE );
            }

            mImageFetcher.loadImage( coverUrl, mHolder.coverImage );

            Log.v( TAG, "bindView : exit" );
        }

        /**
         * Sets the item height. Useful for when we know the column width so the height can be set
         * to match.
         *
         * @param height
         */
//        public void setItemHeight( int height ) {
//
//            if( height == mItemHeight ) {
//                return;
//            }
//
//            mItemHeight = height;
//            //mImageViewLayoutParams = new GridView.LayoutParams( LayoutParams.MATCH_PARENT, mItemHeight );
//            mImageFetcher.setImageSize( height );
//
//            notifyDataSetChanged();
//
//        }
//
//        public void setNumColumns( int numColumns ) {
//
//            mNumColumns = numColumns;
//
//        }
//
//        public int getNumColumns() {
//            return mNumColumns;
//        }

    }

    private static class ViewHolder {

        RecyclingImageView coverImage;
        TextView vip;
        TextView name;

        ViewHolder() { }

    }

}
