package com.keithandthegirl.app.ui;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.keithandthegirl.app.BuildConfig;
import com.keithandthegirl.app.MainApplication;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.LiveConstants;
import com.keithandthegirl.app.db.model.WorkItemConstants;

public abstract class AbstractBaseActivity extends ActionBarActivity {
    private static final String TAG = AbstractBaseActivity.class.getSimpleName();

    private ContentResolver mContentResolver;
    private Uri mUri;
    private BroadcastingObserver mBroadcastingObserver;
    private Drawable micOn, micOff;
    protected Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContentResolver = getContentResolver();

        mUri = new Uri.Builder()
                .scheme("content://")
                .authority(KatgProvider.AUTHORITY)
                .path(WorkItemConstants.TABLE_NAME)
                .build();

        TableObserver observer = new TableObserver();

        mBroadcastingObserver = new BroadcastingObserver();
        mAccount = MainApplication.CreateSyncAccount(this);
        ContentResolver.setSyncAutomatically(mAccount, KatgProvider.AUTHORITY, true);

        micOn = getResources().getDrawable(R.drawable.ic_live_mic_on);
        micOff = getResources().getDrawable(R.drawable.ic_live_mic_off);

        mContentResolver.registerContentObserver(mUri, true, observer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mBroadcastingObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getContentResolver().
                registerContentObserver(
                        ContentUris.withAppendedId(LiveConstants.CONTENT_URI, 1),
                        true,
                        mBroadcastingObserver
                );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        if (BuildConfig.DEBUG) {
            getMenuInflater().inflate(R.menu.debug, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean broadcasting = false;

        Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(LiveConstants.CONTENT_URI, 1), null, null, null, null);
        if (cursor.moveToNext()) {
            broadcasting = cursor.getInt(cursor.getColumnIndex(LiveConstants.FIELD_BROADCASTING)) == 0 ? false : true;
        }
        cursor.close();

        MenuItem broadcastingMenu = menu.findItem(R.id.action_broadcasting);
        if (broadcastingMenu != null) {
            if (broadcasting) {
                broadcastingMenu.setEnabled(true);
                broadcastingMenu.setIcon(micOn);
            } else {
                broadcastingMenu.setEnabled(false);
                broadcastingMenu.setIcon(micOff);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
//            case R.id.action_settings:
//                Intent settingsIntent = new Intent(this, SettingsActivity.class);
//                startActivity(settingsIntent);
//                return true;

//            case R.id.action_login:
//                Intent vipIntent = new Intent(this, AuthenticatorActivity.class);
//                startActivity(vipIntent);
//                return true;

            case R.id.action_work_items:
                Intent workItemsIntent = new Intent(this, WorkItemsActivity.class);
                startActivity(workItemsIntent);
                return true;

            case R.id.action_broadcasting:
                Toast.makeText(this, "KATG is broadcasting now!", Toast.LENGTH_LONG).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class BroadcastingObserver extends ContentObserver {
        private final String TAG = BroadcastingObserver.class.getSimpleName();

        public BroadcastingObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
                super.onChange(selfChange);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            boolean syncActive = ContentResolver.isSyncActive(mAccount, KatgProvider.AUTHORITY);
            boolean syncPending = ContentResolver.isSyncPending(mAccount, KatgProvider.AUTHORITY);

            if (!syncActive && !syncPending) {
                invalidateOptionsMenu();
            }
        }
    }

    public class TableObserver extends ContentObserver {
        private final String TAG = TableObserver.class.getSimpleName();

        public TableObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            boolean syncActive = ContentResolver.isSyncActive(mAccount, KatgProvider.AUTHORITY);
            boolean syncPending = ContentResolver.isSyncPending(mAccount, KatgProvider.AUTHORITY);

            if (!syncActive && !syncPending) {
                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

                ContentResolver.requestSync(mAccount, KatgProvider.AUTHORITY, settingsBundle);
            }
        }
    }

}
