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
import com.keithandthegirl.app.db.model.ShowConstants;
import com.squareup.picasso.Picasso;

/**
 * Created by dmfrey on 3/30/14.
 */
public class ShowHeaderFragment extends Fragment {

    private static final String TAG = ShowHeaderFragment.class.getSimpleName();

    Context mContext;
    ImageView mCoverImageView;
    TextView mTitleTextView, mDescriptionTextView;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.v( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );
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
        super.onActivityCreated(savedInstanceState);

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

    public void updateHeader( long showNameId ) {
        Log.v( TAG, "updateHeader : enter" );

        Log.v( TAG, "updateHeader : showNameId=" + showNameId );

        String[] projection = new String[] { ShowConstants._ID, ShowConstants.FIELD_NAME, ShowConstants.FIELD_DESCRIPTION, ShowConstants.FIELD_COVERIMAGEURL_200 };

        Cursor cursor = mContext.getContentResolver().query( ContentUris.withAppendedId( ShowConstants.CONTENT_URI, showNameId ), projection, null, null, null );
        if( cursor.moveToNext() ) {

            String coverUrl = cursor.getString( cursor.getColumnIndex( ShowConstants.FIELD_COVERIMAGEURL_200 ) );

            mTitleTextView.setText( cursor.getString( cursor.getColumnIndex( ShowConstants.FIELD_NAME ) ) );
            mDescriptionTextView.setText( cursor.getString( cursor.getColumnIndex( ShowConstants.FIELD_DESCRIPTION ) ) );
            Picasso.with(getActivity()).load(coverUrl).fit().centerCrop().into(mCoverImageView);
        }
        cursor.close();

        Log.v( TAG, "updateHeader : exit" );
    }

}
