package com.keithandthegirl.app.ui.shows;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.DatabaseHelper;
import com.keithandthegirl.app.db.model.GuestConstants;
import com.keithandthegirl.app.ui.EpisodeActivity;
import com.squareup.picasso.Picasso;

/**
 * Created by dmfrey on 4/26/14.
 */
public class EpisodeGuestImagesFragment extends Fragment {

    private static final String TAG = EpisodeGuestImagesFragment.class.getSimpleName();

    private static final String RAW_GUESTS_QUERY =
            "SELECT " +
            "    g._id, g.pictureurl " +
            "FROM " +
            "    guest g left join episode_guests eg on g._id = eg.showguestid " +
            "WHERE " +
            "    eg.showid = ?";

    private GridView mGridView;

    DatabaseHelper dbHelper;
    Cursor cursor;
    EpisodeGuestCursorAdapter mAdapter;

    private GuestsObserver guestsObserver = new GuestsObserver();

    private long mEpisodeId;

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
        setRetainInstance( true );

        if( null != getArguments() ) {
            mEpisodeId = getArguments().getLong( EpisodeActivity.EPISODE_KEY );
            Log.v( TAG, "onCreate : mEpisodeId=" + mEpisodeId );
        }
        getActivity().getContentResolver().registerContentObserver( GuestConstants.CONTENT_URI, true, guestsObserver );

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

        mGridView = (GridView) getActivity().findViewById( R.id.episode_guest_images_gridview );

        dbHelper = new DatabaseHelper( getActivity() );
        cursor = dbHelper.getReadableDatabase().rawQuery( RAW_GUESTS_QUERY, new String[] { String.valueOf( mEpisodeId ) } );

//        if( null != cursor ) {

            getActivity().startManagingCursor(cursor);
            mAdapter = new EpisodeGuestCursorAdapter( getActivity(), cursor );

            mGridView.setAdapter( mAdapter );

//        }

        Log.v( TAG, "onActivityCreated : exit" );
    }

    @Override
    public void onDestroy() {
        Log.v( TAG, "onDestroy : enter" );
        super.onDestroy();

        cursor.close();
        dbHelper.close();

        getActivity().getContentResolver().unregisterContentObserver( guestsObserver );

        Log.v( TAG, "onDestroy : exit" );
    }

    private class EpisodeGuestCursorAdapter extends CursorAdapter {

        private final String TAG = EpisodeGuestCursorAdapter.class.getSimpleName();
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
            refHolder.guestImage = (ImageView) view.findViewById( R.id.episode_guest_grid_item_image );
            refHolder.guestImage.setScaleType( ImageView.ScaleType.CENTER_CROP );

            view.setTag( refHolder );

            Log.v( TAG, "newView : exit" );
            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {
            Log.v( TAG, "bindView : enter" );

            ViewHolder mHolder = (ViewHolder) view.getTag();

            String coverUrl = cursor.getString( cursor.getColumnIndex( GuestConstants.FIELD_PICTUREURL ) );
            Log.v(TAG, "bindView : coverUrl=" + coverUrl);
            Picasso.with(getActivity()).load(coverUrl).fit().centerCrop().into(mHolder.guestImage);
            Log.v( TAG, "bindView : exit" );
        }
    }

    private static class ViewHolder {
        ImageView guestImage;
        ViewHolder() { }
    }

    private class GuestsObserver extends ContentObserver {

        public GuestsObserver() {
            super( null );

        }

        @Override
        public void onChange( boolean selfChange ) {

            this.onChange( selfChange, null );

        }

        @Override
        public void onChange( boolean selfChange, Uri uri ) {

            getActivity().runOnUiThread( new Runnable() {

                @Override
                public void run() {

                    mAdapter.notifyDataSetChanged();

                }

            });

        }

    }

}
