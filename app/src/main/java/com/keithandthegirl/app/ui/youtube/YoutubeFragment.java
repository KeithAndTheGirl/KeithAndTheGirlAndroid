package com.keithandthegirl.app.ui.youtube;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import com.keithandthegirl.app.ui.VideoPlayerActivity;
import com.keithandthegirl.app.utils.ImageCache;
import com.keithandthegirl.app.utils.ImageFetcher;

import org.joda.time.DateTime;

/**
 * Created by dmfrey on 4/17/14.
 */
public class YoutubeFragment extends ListFragment {

    private static final String TAG = YoutubeFragment.class.getSimpleName();

    private static final String RAW_VIDEO_QUERY =
            "SELECT " +
             "    e._id as _id, s.name as name, s.prefix as prefix, e.title as title, e.number as number, e.videofileurl as video_url, e.videothumbnailurl as thumbnail, 0 as preview, e.timestamp as timestamp " +
             "FROM " +
             "    episode e left join show s on s._id = e.shownameid " +
             "WHERE " +
             "    e.videofileurl IS NOT NULL AND NOT e.videofileurl = ''" +
             "UNION " +
             "SELECT " +
             "    e._id as _id, s.name as name, s.prefix as prefix, e.title as title, e.number as number, e.previewurl as video_url, \"\" as thumbnail, 1 as preview, e.timestamp as timestamp " +
             "FROM " +
             "    episode e left join show s on s._id = e.shownameid " +
             "WHERE " +
             "    e.previewurl IS NOT NULL AND NOT e.previewurl = ''" +
             "ORDER BY " +
             "    timestamp desc";

    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;

    DatabaseHelper dbHelper;
    Cursor cursor;
    YoutubeCursorAdapter mAdapter;

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

        dbHelper = new DatabaseHelper( getActivity() );
        cursor = dbHelper.getReadableDatabase().rawQuery( RAW_VIDEO_QUERY, null );

        mAdapter = new YoutubeCursorAdapter( getActivity(), cursor );
        setListAdapter( mAdapter );
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume : enter");

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

        String videoUrl = c.getString( c.getColumnIndex( "video_url" ) );
        c.close();

        Intent intent = new Intent( getActivity(), VideoPlayerActivity.class );
        intent.putExtra( VideoPlayerActivity.EPISODE_KEY, id );
        intent.putExtra( VideoPlayerActivity.VIDEO_TYPE_KEY, videoUrl.endsWith( ".m3u8" ) ? "HLS" : "PREVIEW" );
        startActivity( intent );

        Log.v( TAG, "onListItemClick : exit" );
    }


    private class YoutubeCursorAdapter extends CursorAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        public YoutubeCursorAdapter( Context context, Cursor cursor ) {
            super( context, cursor, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {

            View view = mInflater.inflate( R.layout.youtube_item_row, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.thumbnail = (ImageView) view.findViewById( R.id.youtube_thumbnail );
            refHolder.title = (TextView) view.findViewById( R.id.youtube_title );
            refHolder.subTitle = (TextView) view.findViewById( R.id.youtube_subtitle );
            refHolder.select = (ImageView) view.findViewById( R.id.youtube_select );

            view.setTag( refHolder );

            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {

            ViewHolder mHolder = (ViewHolder) view.getTag();

            boolean isPreview = cursor.getInt( cursor.getColumnIndex( "preview" ) ) == 1 ? true : false;

            String preview = "";
            if( isPreview ) {
                preview = " " + getActivity().getString( R.string.youtube_preview );
            }

            mHolder.title.setText( cursor.getString( cursor.getColumnIndex( Show.FIELD_NAME ) ) + " " + cursor.getString( cursor.getColumnIndex( Episode.FIELD_NUMBER ) ) );
            mHolder.subTitle.setText( cursor.getString( cursor.getColumnIndex( Episode.FIELD_TITLE ) ) + preview );

            String thumbnail = cursor.getString( cursor.getColumnIndex( "thumbnail" ) );
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
        TextView subTitle;
        ImageView select;

        ViewHolder() { }

    }

}
