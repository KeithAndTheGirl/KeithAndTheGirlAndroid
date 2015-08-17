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
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.keithandthegirl.app.BuildConfig;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.LiveConstants;
import com.keithandthegirl.app.db.model.WorkItemConstants;
import com.keithandthegirl.app.ui.main.AboutFragment;
import com.keithandthegirl.app.ui.main.EventCardsFragment;
import com.keithandthegirl.app.ui.main.EventsFragment;
import com.keithandthegirl.app.ui.main.GuestCardsFragment;
import com.keithandthegirl.app.ui.main.GuestsFragment;
import com.keithandthegirl.app.ui.main.LiveFragment;
import com.keithandthegirl.app.ui.main.ShowsTabFragment;
import com.keithandthegirl.app.ui.main.YoutubeFragment;
import com.keithandthegirl.app.ui.player.PlaybackStatusFragment;
import com.keithandthegirl.app.ui.settings.SettingsActivity;
import com.keithandthegirl.app.utils.NetworkUtils;

import timber.log.Timber;

public abstract class AbstractBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ContentResolver mContentResolver;
    private Uri mUri;
    private Drawable micOn, micOff;

    protected NavigationView mNavigationView;
    protected DrawerLayout mDrawerLayout;
    private Button mVipButton;

    private PlaybackStatusFragment mPlaybackControlsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (null != mNavigationView) {
            mNavigationView.setNavigationItemSelectedListener(this);

            mVipButton = (Button) findViewById(R.id.vipButton);
            mVipButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent settingsIntent = new Intent(AbstractBaseActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                }

            });
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (null != toolbar) {
//            setSupportActionBar(toolbar);
//
//            final ActionBar actionBar = getSupportActionBar();
//            if (null != actionBar) {
//
//                if (null != mNavigationView) {
//
//                    actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
//                }
//
//                actionBar.setDisplayHomeAsUpEnabled(true);
//            }
//
//        }

        Intent alarmIntent = new Intent(this, com.keithandthegirl.app.sync.KatgAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);

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
    protected void onStart() {
        super.onStart();

        mPlaybackControlsFragment = (PlaybackStatusFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback_controls);
        if (null == mPlaybackControlsFragment) {
            throw new IllegalStateException("Mising fragment with id 'controls'. Cannot continue.");
        }

        hidePlaybackControls();
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
            case android.R.id.home:
                if (null != mNavigationView) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    return true;
                }
            case R.id.action_broadcasting:
                Toast.makeText(this, "KATG is broadcasting now!", Toast.LENGTH_LONG).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        Timber.i("onNavigationItemSelected : enter - " + menuItem.getTitle());

        if (menuItem.isChecked()) {
            Timber.i("onNavigationItemSelected : menuItem already checked, close it");

            mDrawerLayout.closeDrawer(GravityCompat.START);

            return false;
        }

        menuItem.setChecked(true);
        mDrawerLayout.closeDrawer(GravityCompat.START);

        switch (menuItem.getItemId()) {

            case R.id.menu_shows:
                setTitle("Shows");
                replaceFragment(ShowsTabFragment.newInstance());
                return true;
            case R.id.menu_guests:
                setTitle(getResources().getString(R.string.menu_item_guests));
                replaceFragment(GuestCardsFragment.newInstance());
                return true;
            case R.id.menu_live:
                setTitle(getResources().getString(R.string.menu_item_live));
                replaceFragment(LiveFragment.newInstance());
                return true;
            case R.id.menu_schedule:
                setTitle(getResources().getString(R.string.menu_item_schedule));
                replaceFragment(EventCardsFragment.newInstance());
                return true;
            case R.id.menu_youtube:
                setTitle(getResources().getString(R.string.menu_item_youtube));
                replaceFragment(YoutubeFragment.newInstance());
                return true;
            case R.id.menu_about:
                setTitle(getResources().getString(R.string.menu_item_about));
                replaceFragment(AboutFragment.newInstance());
                return true;
            case R.id.menu_settings:
                setTitle(getResources().getString(R.string.menu_item_settings));
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return false;
    }

    protected void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    protected void showPlaybackControls() {
        if (NetworkUtils.isOnline(this)) {
            getSupportFragmentManager().beginTransaction()
//                    .setCustomAnimations(
//                            R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
//                            R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom )
                    .show(mPlaybackControlsFragment)
                    .commit();
        }
    }

    protected void hidePlaybackControls() {
        getSupportFragmentManager().beginTransaction()
                .hide(mPlaybackControlsFragment)
                .commit();
    }
}
