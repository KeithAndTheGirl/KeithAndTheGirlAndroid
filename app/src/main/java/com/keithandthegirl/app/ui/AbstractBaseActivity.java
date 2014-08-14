package com.keithandthegirl.app.ui;


import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.keithandthegirl.app.MainApplication;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.LiveConstants;
import com.keithandthegirl.app.db.model.WorkItemConstants;
import com.keithandthegirl.app.sync.SyncAdapter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public abstract class AbstractBaseActivity extends ActionBarActivity {

    private static final String TAG = AbstractBaseActivity.class.getSimpleName();

    private ContentResolver mContentResolver;
    private Uri mUri;

    private LayoutInflater mInflater;
    private BroadcastingObserver mBroadcastingObserver;

    private Drawable micOn, micOff;

    protected Account mAccount;

    protected MenuItem refreshItem;
    protected ImageView refreshImageView;
    protected Animation mRefreshRotation;

    private SyncStartReceiver mSyncStartReceiver = new SyncStartReceiver();
    private SyncCompleteReceiver mSyncCompleteReceiver = new SyncCompleteReceiver();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        Log.d( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        mContentResolver = getContentResolver();

        mUri = new Uri.Builder()
                .scheme( "content://" )
                .authority( KatgProvider.AUTHORITY )
                .path( WorkItemConstants.TABLE_NAME )
                .build();

        TableObserver observer = new TableObserver();

        mBroadcastingObserver = new BroadcastingObserver();

        mAccount = MainApplication.CreateSyncAccount( this );
        ContentResolver.setSyncAutomatically( mAccount, KatgProvider.AUTHORITY, true );

        micOn = getResources().getDrawable( R.drawable.ic_live_mic_on );
        micOff = getResources().getDrawable( R.drawable.ic_live_mic_off );

        mContentResolver.registerContentObserver( mUri, true, observer );

        mInflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        refreshImageView = (ImageView) mInflater.inflate( R.layout.refresh_action_view, null );

        mRefreshRotation = AnimationUtils.loadAnimation( this, R.anim.clockwise_refresh );
        mRefreshRotation.setRepeatCount( Animation.INFINITE );

        Log.d( TAG, "onCreate : exit" );
    }

    @Override
    protected void onPause() {
        Log.d( TAG, "onPause : enter" );
        super.onPause();

        if( null != mSyncStartReceiver ) {
            unregisterReceiver( mSyncStartReceiver );
        }

        if( null != mSyncCompleteReceiver ) {
            unregisterReceiver( mSyncCompleteReceiver );
        }

        getContentResolver().unregisterContentObserver( mBroadcastingObserver );

        Log.d( TAG, "onPause : exit" );
    }

    @Override
    protected void onResume() {
        Log.d( TAG, "onResume : enter" );
        super.onResume();

        IntentFilter syncStartIntentFilter = new IntentFilter( SyncAdapter.START_ACTION );
        registerReceiver( mSyncStartReceiver, syncStartIntentFilter );

        IntentFilter syncCompleteIntentFilter = new IntentFilter( SyncAdapter.COMPLETE_ACTION );
        registerReceiver( mSyncCompleteReceiver, syncCompleteIntentFilter );

        getContentResolver().
            registerContentObserver(
                    ContentUris.withAppendedId(LiveConstants.CONTENT_URI, 1),
                    true,
                    mBroadcastingObserver
            );

        boolean syncActive = ContentResolver.isSyncActive( mAccount, KatgProvider.AUTHORITY );

        if( syncActive ) {

            refresh();

        }

        Log.d( TAG, "onResume : exit" );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        Log.d( TAG, "onCreateOptionsMenu : enter" );

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main, menu );

        Log.d( TAG, "onCreateOptionsMenu : exit" );
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu( Menu menu ) {
        Log.d( TAG, "onPrepareOptionsMenu : enter" );
        super.onPrepareOptionsMenu( menu );

        boolean broadcasting = false;

        Cursor cursor = getContentResolver().query( ContentUris.withAppendedId( LiveConstants.CONTENT_URI, 1 ), null, null, null, null );
        if( cursor.moveToNext() ) {

            broadcasting = cursor.getInt( cursor.getColumnIndex( LiveConstants.FIELD_BROADCASTING ) ) == 0 ? false : true;

        }
        cursor.close();


        if( broadcasting ) {

            menu.findItem( R.id.action_broadcasting ).setEnabled( true );
            menu.findItem( R.id.action_broadcasting ).setIcon( micOn );

        } else {

            menu.findItem( R.id.action_broadcasting ).setEnabled( false );
            menu.findItem( R.id.action_broadcasting ).setIcon( micOff );

        }

        boolean syncActive = ContentResolver.isSyncActive( mAccount, KatgProvider.AUTHORITY );

        if( syncActive ) {

            refresh();

        }

        Log.d( TAG, "onPrepareOptionsMenu : exit" );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if( id == R.id.action_settings ) {
            return true;
        }

        switch( id ) {

            case R.id.action_settings :

                Intent preferencesIntent = new Intent( this, PreferencesActivity.class );
                startActivity( preferencesIntent );

                return true;

            case R.id.action_login :

                Intent vipIntent = new Intent( this, AuthenticatorActivity.class );
                startActivity( vipIntent );

                return true;

            case R.id.action_work_items :

                Intent workItemsIntent = new Intent( this, WorkItemsActivity.class );
                startActivity( workItemsIntent );

                return true;

            case R.id.action_broadcasting :

                Toast.makeText( this, "KATG is broadcasting now!", Toast.LENGTH_LONG ).show();

                return true;

            case R.id.action_refresh :

                refreshItem = item;

                Bundle b = new Bundle();
                b.putBoolean( ContentResolver.SYNC_EXTRAS_MANUAL, true );
                b.putBoolean( ContentResolver.SYNC_EXTRAS_EXPEDITED, true );

                ContentResolver.setSyncAutomatically( mAccount, KatgProvider.AUTHORITY, true );
                ContentResolver.setIsSyncable( mAccount, KatgProvider.AUTHORITY, 1);

                boolean pending = ContentResolver.isSyncPending( mAccount, KatgProvider.AUTHORITY );
                boolean active = ContentResolver.isSyncActive( mAccount, KatgProvider.AUTHORITY );

                if (pending || active) {
                    Log.d( TAG, "Cancelling previously pending/active sync." );
                    ContentResolver.cancelSync( mAccount, KatgProvider.AUTHORITY );
                }

                DateTime now = new DateTime( DateTimeZone.UTC );
                now = now.minusDays( 1 );

                ContentValues values = new ContentValues();
                values.put( WorkItemConstants.FIELD_LAST_RUN, now.getMillis() );

                int updated = getContentResolver().update( WorkItemConstants.CONTENT_URI, values, WorkItemConstants.FIELD_FREQUENCY + " = ? OR " + WorkItemConstants.FIELD_FREQUENCY + " = ?", new String[]{ WorkItemConstants.Frequency.HOURLY.name(), WorkItemConstants.Frequency.DAILY.name() } );
                Log.i( TAG, "onOptionsItemSelected : records updated=" + updated );

//                ContentResolver.requestSync( mAccount, KatgProvider.AUTHORITY, b );

                return true;
        }

        return super.onOptionsItemSelected( item );
    }

    public void refresh() {
        Log.d( TAG, "refresh : enter" );

        if( null != refreshImageView && null != refreshItem ) {
            Log.d( TAG, "refresh : starting animation" );

           /* Attach a rotating ImageView to the refresh item as an ActionView */

           refreshImageView.startAnimation( mRefreshRotation );

           refreshItem.setActionView( refreshImageView );

        }

        Log.d( TAG, "refresh : exit" );
    }

    public void refreshComplete() {
        Log.d( TAG, "refreshComplete : enter" );

        if( null != refreshItem && null != refreshItem.getActionView() ) {
            Log.d( TAG, "refreshComplete : cleaning up animation" );

            refreshItem.getActionView().clearAnimation();
            refreshItem.setActionView( null );

            invalidateOptionsMenu();
        }

        Log.d( TAG, "refreshComplete : exit" );
    }

    @SuppressLint( "NewApi" )
    private class BroadcastingObserver extends ContentObserver {

        private final String TAG = BroadcastingObserver.class.getSimpleName();

        public BroadcastingObserver() {
            super( null );
        }

        @Override
        public void onChange( boolean selfChange ) {
            super.onChange( selfChange, null );
        }

        @Override
        public void onChange( boolean selfChange, Uri uri ) {
            Log.i( TAG, "onChange : enter" );

            boolean syncActive = ContentResolver.isSyncActive( mAccount, KatgProvider.AUTHORITY );
            boolean syncPending = ContentResolver.isSyncPending( mAccount, KatgProvider.AUTHORITY);

            if( !syncActive && !syncPending ) {
                invalidateOptionsMenu();
            }

            Log.i( TAG, "onChange : exit" );
        }

    }

    public class TableObserver extends ContentObserver {

        private final String TAG = TableObserver.class.getSimpleName();

        public TableObserver() {
            super( null );
        }

        @Override
        public void onChange( boolean selfChange ) {
            Log.i( TAG, "onChange : enter" );
            super.onChange( selfChange );

            onChange( selfChange, null );

            Log.i( TAG, "onChange : exit" );
        }

        @Override
        public void onChange( boolean selfChange, Uri uri ) {
            Log.i( TAG, "onChange : enter" );

            boolean syncActive = ContentResolver.isSyncActive( mAccount, KatgProvider.AUTHORITY );
            boolean syncPending = ContentResolver.isSyncPending( mAccount, KatgProvider.AUTHORITY);

            if( !syncActive && !syncPending ) {

                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_MANUAL, true );
                settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_EXPEDITED, true );

                ContentResolver.requestSync( mAccount, KatgProvider.AUTHORITY, settingsBundle );

            }

            Log.i( TAG, "onChange : exit" );
        }

    }

    private class SyncStartReceiver extends BroadcastReceiver {

        private final String TAG = SyncStartReceiver.class.getSimpleName();

        @Override
        public void onReceive( Context context, Intent intent ) {
            Log.d( TAG, "onReceive : enter" );

            if( intent.getAction().equals( SyncAdapter.START_ACTION ) ) {
                Log.v( TAG, "onReceive : sync started" );

                refresh();

            }

            Log.d( TAG, "onReceive : exit" );
        }

    }

    private class SyncCompleteReceiver extends BroadcastReceiver {

        private final String TAG = SyncCompleteReceiver.class.getSimpleName();

        @Override
        public void onReceive( Context context, Intent intent ) {
            Log.d( TAG, "onReceive : enter" );

            if( intent.getAction().equals( SyncAdapter.COMPLETE_ACTION ) ) {
                Log.v( TAG, "onReceive : sync complete" );

                refreshComplete();

            }

            Log.d( TAG, "onReceive : exit" );
        }

    }

}
