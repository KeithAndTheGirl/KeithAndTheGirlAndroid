package com.keithandthegirl.app.ui.episodes;

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
import com.keithandthegirl.app.db.model.ImageConstants;
import com.squareup.picasso.Picasso;

/**
 * Created by dmfrey on 4/30/14.
 */
public class EpisodeImagesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = EpisodeImagesFragment.class.getSimpleName();

    private int mImageThumbSize;
    private GridView mGridViewImages;
    private long mEpisodeId;
    EpisodeImageCursorAdapter mAdapter;

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        String[] projection = { ImageConstants._ID, ImageConstants.FIELD_MEDIAURL };

        String selection = ImageConstants.FIELD_SHOWID + " = ?";

        String[] selectionArgs = new String[] { String.valueOf( mEpisodeId ) };

        CursorLoader cursorLoader = new CursorLoader( getActivity(), ImageConstants.CONTENT_URI, projection, selection, selectionArgs, ImageConstants.FIELD_DISPLAY_ORDER );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished( Loader<Cursor> cursorLoader, Cursor cursor ) {
        mAdapter.swapCursor( cursor );
    }

    @Override
    public void onLoaderReset( Loader<Cursor> cursorLoader ) {
        mAdapter.swapCursor(null);
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
        super.onCreate( savedInstanceState );

        setRetainInstance(true);

        if( null != getArguments() ) {
            mEpisodeId = getArguments().getLong( EpisodeActivity.EPISODE_KEY );
        }
        mImageThumbSize = getResources().getDimensionPixelSize( R.dimen.image_thumbnail_size );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        final View rootView = inflater.inflate( R.layout.fragment_episode_images, container, false );
        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        getLoaderManager().initLoader( 0, getArguments(), this );

        mAdapter = new EpisodeImageCursorAdapter( getActivity() );

        mGridViewImages = (GridView) getActivity().findViewById( R.id.episode_images_gridview );
        mGridViewImages.setAdapter( mAdapter );
//        mGridViewImages.setOnItemClickListener( this );
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private class EpisodeImageCursorAdapter extends CursorAdapter {
        private final String TAG = EpisodeImageCursorAdapter.class.getSimpleName();

        private final Context mContext;
        private LayoutInflater mInflater;
        private int mLastKnownCount = -1;

        public EpisodeImageCursorAdapter( Context context ) {
            super( context, null, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );

        }

        /**
         * Sets the height of mGridViewImages based on item height and row count.
         */
        private void setGridViewHeight(){
            if(mGridViewImages == null) return;

            //get image count
            final int imageCount = mAdapter.getCount();

            //determine row count
            int rowCount = imageCount / mGridViewImages.getNumColumns();

            //account for partial rows
            if((float)imageCount%(float)mGridViewImages.getNumColumns() > 0) rowCount++;

            //determine the height of 1 row
            int verticalSpacing = (int)getResources().getDimension(R.dimen.episode_images_gridview_vertical_spacing);
            final int rowHeight = mImageThumbSize + verticalSpacing;    //this requires API 16 -- final int rowHeight = mImageThumbSize + mGridViewImages.getVerticalSpacing();

            //set gridview height
            ViewGroup.LayoutParams params = mGridViewImages.getLayoutParams();
            params.height =  (rowCount * rowHeight) + mGridViewImages.getPaddingTop() + mGridViewImages.getPaddingBottom();
            mGridViewImages.setLayoutParams(params);

            mLastKnownCount = mAdapter.getCount();
        }

        /**
         * If the content changes resize the gridview's height
         */
        @Override
        protected void onContentChanged() {
            super.onContentChanged();
            setGridViewHeight();
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {
            //resize the gridview height based on item count.
            if(mAdapter.getCount() != mLastKnownCount) setGridViewHeight();

            View view = mInflater.inflate( R.layout.episode_image_grid_item, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.mediaUrl = (ImageView) view.findViewById( R.id.episode_grid_item_image );
//            refHolder.mediaUrl.setScaleType( ImageView.ScaleType.CENTER_CROP );

            view.setTag( refHolder );
            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {
            ViewHolder mHolder = (ViewHolder) view.getTag();

            String mediaUrl = cursor.getString( cursor.getColumnIndex( ImageConstants.FIELD_MEDIAURL ) );
            Log.d( TAG, "bindView : mediaUrl=" + mediaUrl );
            Picasso.with(getActivity()).load(mediaUrl).fit().centerCrop().into(mHolder.mediaUrl);
        }
    }

    private static class ViewHolder {
        ImageView mediaUrl;

        ViewHolder() { }
    }

}
