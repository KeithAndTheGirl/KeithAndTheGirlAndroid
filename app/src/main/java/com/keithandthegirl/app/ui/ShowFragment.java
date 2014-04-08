package com.keithandthegirl.app.ui;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.EpisodeGuests;
import com.keithandthegirl.app.db.model.Guest;
import com.keithandthegirl.app.utils.ImageUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by dmfrey on 3/30/14.
 */
public class ShowFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ShowFragment.class.getSimpleName();

    public static final String SHOW_NAME_ID_KEY = "showNameId";

    EpisodeCursorAdapter mAdapter;
    long mShowNameId;

    /**
     * Returns a new instance of this fragment for the given show id.
     */
    public static ShowFragment newInstance( long showNameId ) {

        ShowFragment fragment = new ShowFragment();

        Bundle args = new Bundle();
        args.putLong( SHOW_NAME_ID_KEY, showNameId );
        fragment.setArguments( args );

        return fragment;
    }

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        Log.v(TAG, "onCreateLoader : enter");

        String[] projection = null;

        String selection = Episode.FIELD_SHOWNAMEID + "=?";

        mShowNameId = args.getLong( SHOW_NAME_ID_KEY );
        String[] selectionArgs = new String[] { String.valueOf( mShowNameId ) };

        CursorLoader cursorLoader = new CursorLoader( getActivity(), Episode.CONTENT_URI, projection, selection, selectionArgs, Episode.FIELD_NUMBER + " DESC" );

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
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        Log.v( TAG, "onCreateView : enter" );

        View rootView = inflater.inflate( R.layout.fragment_show, container, false );

        Log.v( TAG, "onCreateView : exit" );
        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.v( TAG, "onActivityCreated : enter" );
        super.onActivityCreated( savedInstanceState );

        if( null != getArguments() ) {

            long showNameId = getArguments().getLong( SHOW_NAME_ID_KEY );

            updateShow( showNameId );

            mAdapter = new EpisodeCursorAdapter( getActivity().getApplicationContext() );
            setListAdapter( mAdapter );

        }

        Log.v( TAG, "onActivityCreated : exit" );
    }


    public void updateShow( long showNameId ) {
        Log.v( TAG, "updateShow : enter" );

        Log.v( TAG, "updateShow : showNameId=" + showNameId );

        Bundle args = new Bundle();
        args.putLong( SHOW_NAME_ID_KEY, showNameId );
        getLoaderManager().restartLoader( 0, args, this );

        ShowHeaderFragment showHeaderFragment = (ShowHeaderFragment) getChildFragmentManager().findFragmentById( R.id.show_header );
        if( null != showHeaderFragment ) {
            Log.v( TAG, "updateShow : updating show header" );

            showHeaderFragment.updateHeader( showNameId );

        } else {
            Log.v( TAG, "updateShow : adding show header" );

            // Create fragment and give it an argument for the selected article
            ShowHeaderFragment newFragment = new ShowHeaderFragment();
            newFragment.setArguments( args );

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace( R.id.show_header, newFragment );
            //transaction.addToBackStack( null );

            // Commit the transaction
            transaction.commit();
        }

        Log.v( TAG, "updateShow : exit" );
    }

    private class EpisodeCursorAdapter extends CursorAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        String mEpisodesLabel;
        DateTimeFormatter mFormatter = DateTimeFormat.forPattern( "MMM d, yyyy" ).withZone( DateTimeZone.forTimeZone( TimeZone.getTimeZone( "America/New_York" ) ) );

        public EpisodeCursorAdapter( Context context ) {
            super( context, null, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );

            mEpisodesLabel = mContext.getResources().getString( R.string.episode_label );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {

            View view = mInflater.inflate( R.layout.episode_row, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.number = (TextView) view.findViewById( R.id.episode_number );
            refHolder.showDate = (TextView) view.findViewById( R.id.episode_date );
            refHolder.title = (TextView) view.findViewById( R.id.episode_title );
            refHolder.details = (ImageView) view.findViewById( R.id.episode_details );
//            refHolder.guestNames = (TextView) view.findViewById( R.id.episode_guest_names );
//            refHolder.guestImages = (LinearLayout) view.findViewById( R.id.episode_guest_images );
            refHolder.played = (TextView) view.findViewById( R.id.episode_played );
            refHolder.downloaded = (TextView) view.findViewById( R.id.episode_downloaded );

            view.setTag( refHolder );

            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {

            ViewHolder mHolder = (ViewHolder) view.getTag();

            long id = cursor.getLong( cursor.getColumnIndex( Episode._ID ) );
            long instant = cursor.getLong( cursor.getColumnIndex( Episode.FIELD_TIMESTAMP ) );

            mHolder.number.setText( mEpisodesLabel + " " + cursor.getInt( cursor.getColumnIndex( Episode.FIELD_NUMBER ) ) );
            mHolder.showDate.setText( mFormatter.print( instant ) );
            mHolder.title.setText(cursor.getString( cursor.getColumnIndex( Episode.FIELD_TITLE ) ) );

//            List<Long> episodeGuests = new ArrayList<Long>();
//            Cursor episodeGuestCursor = mContext.getContentResolver().query( EpisodeGuests.CONTENT_URI, null, EpisodeGuests.FIELD_SHOWID + "=?", new String[] { String.valueOf( id ) }, EpisodeGuests.FIELD_SHOWGUESTID );
//            while( episodeGuestCursor.moveToNext() ) {
//                episodeGuests.add( episodeGuestCursor.getLong( episodeGuestCursor.getColumnIndex( EpisodeGuests.FIELD_SHOWGUESTID ) ) );
//            }
//            episodeGuestCursor.close();
//
//            if( !episodeGuests.isEmpty() ) {
//
//                int index = 0;
//                String guestLabel = "";
//
//                for( long guestId : episodeGuests ) {
//
//                    Cursor guestCursor = mContext.getContentResolver().query( ContentUris.withAppendedId( Guest.CONTENT_URI, guestId ), null, null, null, null );
//                    while( guestCursor.moveToNext() ) {
//
//                        guestLabel = guestLabel + guestCursor.getString( guestCursor.getColumnIndex( Guest.FIELD_REALNAME ) );
//                    }
//                    guestCursor.close();
//
//                    String filename = "guest_" + guestId + "_150x150.jpg";
//
//                    if( mContext.getFileStreamPath( filename ).exists() ) {
//
//                        String path = mContext.getFileStreamPath( filename ).getAbsolutePath();
//
//                        BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inJustDecodeBounds = true;
//                        BitmapFactory.decodeFile( path, options);
//                        int imageHeight = options.outHeight;
//                        int imageWidth = options.outWidth;
//                        String imageType = options.outMimeType;
//
//                        float aspectRatio = imageWidth / imageHeight;
//                        float newWidth = imageWidth * 75 / imageHeight;
//
//                        Log.v( TAG, "updateHeader : original image info (hxw) - " + imageHeight + "x" + imageWidth + ", aspectRatio = " + aspectRatio + ", new size (hxw) - 150x" + newWidth + ", " + imageType );
//
//                        ImageView guestImage = new ImageView( mContext );
//                        guestImage.setImageBitmap( ImageUtils.decodeSampledBitmapFromFile( path, (int) newWidth, 75 ) );
//                        guestImage.setPadding( 0, 0, 10, 0 );
//                        mHolder.guestImages.addView( guestImage );
//
//                    }
//
//                    if( index < episodeGuests.size() - 1 ) {
//                        guestLabel = guestLabel + ", ";
//                    }
//
//                    index++;
//
//                }
//
//                if( !"".equals( guestLabel ) ) {
//
//                    mHolder.guestNames.setText( guestLabel );
//
//                }
//
//            }

        }

    }

    private static class ViewHolder {

        TextView number;
        TextView showDate;
        TextView title;
        ImageView details;
//        TextView guestNames;
//        LinearLayout guestImages;
        TextView played;
        TextView downloaded;

        ViewHolder() { }

    }

}
