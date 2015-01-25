package com.keithandthegirl.app.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.keithandthegirl.app.BuildConfig;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.LiveConstants;
import com.keithandthegirl.app.db.model.WorkItemConstants;

public abstract class AbstractBaseActivity extends ActionBarActivity {

    private static final String TAG = AbstractBaseActivity.class.getSimpleName();

    private ContentResolver mContentResolver;
    private Uri mUri;
    private Drawable micOn, micOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent alarmIntent = new Intent( this, com.keithandthegirl.app.sync.KatgAlarmReceiver.class );
        PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 0, alarmIntent, 0 );
        AlarmManager manager = (AlarmManager) this.getSystemService( Context.ALARM_SERVICE );
        manager.setInexactRepeating( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent );

        mContentResolver = getContentResolver();

        mUri = new Uri.Builder()
                .scheme("content://")
                .authority(KatgProvider.AUTHORITY)
                .path(WorkItemConstants.TABLE_NAME)
                .build();

        micOn = getResources().getDrawable(R.drawable.ic_live_mic_on);
        micOff = getResources().getDrawable(R.drawable.ic_live_mic_off);

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

            case R.id.action_broadcasting:
                Toast.makeText(this, "KATG is broadcasting now!", Toast.LENGTH_LONG).show();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

}
