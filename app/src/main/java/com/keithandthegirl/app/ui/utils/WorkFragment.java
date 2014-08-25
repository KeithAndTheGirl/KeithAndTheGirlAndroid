package com.keithandthegirl.app.ui.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.WorkItemConstants;
import com.keithandthegirl.app.sync.SyncAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by dmfrey on 3/21/14.
 */
public class WorkFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = WorkFragment.class.getSimpleName();

    private SyncCompleteReceiver mSyncCompleteReceiver = new SyncCompleteReceiver();

    WorkItemCursorAdapter mAdapter;

    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        String[] projection = null;

        String selection = null;

        String[] selectionArgs = null;

        CursorLoader cursorLoader = new CursorLoader( getActivity(), WorkItemConstants.CONTENT_URI, projection, selection, selectionArgs, null );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished( Loader<Cursor> cursorLoader, Cursor cursor ) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset( Loader<Cursor> cursorLoader ) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader( 0, getArguments(), this );
        mAdapter = new WorkItemCursorAdapter( getActivity().getApplicationContext() );
        setListAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();

        if( null != mSyncCompleteReceiver ) {
            getActivity().unregisterReceiver( mSyncCompleteReceiver );
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter syncCompleteIntentFilter = new IntentFilter( SyncAdapter.COMPLETE_ACTION );
        getActivity().registerReceiver( mSyncCompleteReceiver, syncCompleteIntentFilter );
    }

    private class WorkItemCursorAdapter extends CursorAdapter {
        private final DateTimeFormatter fmt = DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss" );

        private Context mContext;
        private LayoutInflater mInflater;

        public WorkItemCursorAdapter( Context context ) {
            super( context, null, false );

            mContext = context;
            mInflater = LayoutInflater.from( context );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {

            View view = mInflater.inflate( R.layout.work_item_row, parent, false );

            ViewHolder refHolder = new ViewHolder();
            refHolder.name = (TextView) view.findViewById( R.id.work_item_name );
            refHolder.status = (TextView) view.findViewById( R.id.work_item_status );
            refHolder.frequency = (TextView) view.findViewById( R.id.work_item_frequency );
            refHolder.lastRun = (TextView) view.findViewById( R.id.work_item_last_run );

            view.setTag( refHolder );

            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {

            ViewHolder mHolder = (ViewHolder) view.getTag();

            mHolder.name.setText( cursor.getString( cursor.getColumnIndex( WorkItemConstants.FIELD_NAME ) ) );
            mHolder.status.setText( cursor.getString( cursor.getColumnIndex( WorkItemConstants.FIELD_STATUS ) ) );
            mHolder.frequency.setText( cursor.getString( cursor.getColumnIndex( WorkItemConstants.FIELD_FREQUENCY ) ) );

            long instant = cursor.getLong( cursor.getColumnIndex( WorkItemConstants.FIELD_LAST_RUN ) );
            if( instant < 0 ) {
                mHolder.lastRun.setText( "" );
            } else {
                DateTime lastRun = new DateTime( instant );
                mHolder.lastRun.setText( fmt.print( lastRun ) );
            }
        }

    }

    private static class ViewHolder {

        TextView name;
        TextView status;
        TextView frequency;
        TextView lastRun;

        ViewHolder() { }

    }

    private class SyncCompleteReceiver extends BroadcastReceiver {

        private final String TAG = SyncCompleteReceiver.class.getSimpleName();

        @Override
        public void onReceive( Context context, Intent intent ) {
            if( intent.getAction().equals( SyncAdapter.COMPLETE_ACTION ) ) {
                Log.v( TAG, "onReceive : sync complete" );

                mAdapter.notifyDataSetChanged();

            }
        }
    }
}
