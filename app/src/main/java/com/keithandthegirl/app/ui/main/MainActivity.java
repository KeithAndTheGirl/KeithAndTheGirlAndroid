package com.keithandthegirl.app.ui.main;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.keithandthegirl.app.BuildConfig;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.LiveConstants;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.ui.player.PlaybackStatusFragment;
import com.keithandthegirl.app.ui.settings.SettingsActivity;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private PlaybackStatusFragment mPlaybackControlsFragment;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView(R.layout.activity_main);

        boolean neverRun = false;
        Cursor cursor = getContentResolver().query( ShowConstants.CONTENT_URI, null, null, null, null );
        if( cursor.getCount() == 0 ) {
            neverRun = true;
        }
        cursor.close();

        if( neverRun ) {
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_MANUAL, true );
            settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_EXPEDITED, true );
        }

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
        }

        setupNavigationDrawer();

        Intent alarmIntent = new Intent(this, com.keithandthegirl.app.sync.KatgAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, ShowsTabFragment.newInstance())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPlaybackControlsFragment = (PlaybackStatusFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback_controls);
        if (null == mPlaybackControlsFragment) {
            throw new IllegalStateException("Missing fragment with id 'controls'. Cannot continue.");
        }

        hidePlaybackControls();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
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
                broadcastingMenu.setIcon(R.drawable.ic_live_mic_on);
            } else {
                broadcastingMenu.setIcon(R.drawable.ic_live_mic_off);
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
                replaceFragment(LiveFragment.newInstance());
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
                replaceFragment(GuestsFragment.newInstance());
                return true;
            case R.id.menu_live:
                setTitle(getResources().getString(R.string.menu_item_live));
                replaceFragment(LiveFragment.newInstance());
                return true;
            case R.id.menu_schedule:
                setTitle(getResources().getString(R.string.menu_item_schedule));
                replaceFragment(EventsFragment.newInstance());
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

    private void setupNavigationDrawer() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }


    public void onVipButtonClicked() {
        Intent settingsIntent = new Intent( this, SettingsActivity.class );
        startActivity(settingsIntent);
    }

    private void hidePlaybackControls() {
        getSupportFragmentManager().beginTransaction()
                .hide(mPlaybackControlsFragment)
                .commit();
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }

}
