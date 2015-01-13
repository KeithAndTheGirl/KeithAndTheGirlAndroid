package com.keithandthegirl.app.ui.main;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.db.schedule.KatgAlarmReceiver;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.episode.EpisodeFragment;
import com.keithandthegirl.app.ui.navigationdrawer.NavigationDrawerFragment;
import com.keithandthegirl.app.ui.navigationdrawer.NavigationItem;
import com.keithandthegirl.app.ui.player.PlaybackStatusFragment;
import com.keithandthegirl.app.ui.settings.SettingsActivity;
import com.keithandthegirl.app.ui.shows.ShowFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity
        extends AbstractBaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                   ShowFragment.OnShowFragmentListener, PlaybackStatusFragment.PlayerVisibilityListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    KatgAlarmReceiver alarm = new KatgAlarmReceiver();

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private PlaybackStatusFragment mPlayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setTitle(R.string.katg_actionbar_title);

        mNavigationDrawerFragment =
                (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment.setupDrawer(R.id.navigation_drawer, drawerLayout, getNavigationItemList());

        mPlayerFragment =
                (PlaybackStatusFragment) getSupportFragmentManager().findFragmentById(R.id.katgToolbarPlayer);
        mPlayerFragment.setPlayerVisibilityListener(this);
        if (mPlayerFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(mPlayerFragment).commit();
        }

        boolean neverRun = false;
        Cursor cursor = getContentResolver().query(ShowConstants.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() == 0) {
            neverRun = true;
        }
        cursor.close();

        if (neverRun) {
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

            Log.v(TAG, "onCreate : requesting sync");
            ContentResolver.requestSync(mAccount, KatgProvider.AUTHORITY, settingsBundle);
        }

        alarm.setAlarm(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private List<NavigationItem> getNavigationItemList() {
        List<NavigationItem> navigationItemList = new ArrayList<>();

        NavigationItem showsNavigationItem = new NavigationItem(NavigationItem.NavigationItemType.SHOWS);
        showsNavigationItem.setIcon(R.drawable.ic_tab_shows_off);
        showsNavigationItem.setLabel("Shows");
        navigationItemList.add(showsNavigationItem);

        NavigationItem guestsNavigationItem = new NavigationItem(NavigationItem.NavigationItemType.GUESTS);
        guestsNavigationItem.setIcon(R.drawable.ic_tab_guest_off);
        guestsNavigationItem.setLabel("Guests");
        navigationItemList.add(guestsNavigationItem);

        NavigationItem liveNavigationItem = new NavigationItem(NavigationItem.NavigationItemType.LIVE);
        liveNavigationItem.setIcon(R.drawable.ic_tab_live_off);
        liveNavigationItem.setLabel("Live");
        navigationItemList.add(liveNavigationItem);

        NavigationItem scheduleNavigationItem = new NavigationItem(NavigationItem.NavigationItemType.SCHEDULE);
        scheduleNavigationItem.setIcon(R.drawable.ic_tab_calendar_off);
        scheduleNavigationItem.setLabel("Schedule");
        navigationItemList.add(scheduleNavigationItem);

        NavigationItem youtubeNavigationItem = new NavigationItem(NavigationItem.NavigationItemType.YOUTUBE);
        youtubeNavigationItem.setIcon(R.drawable.ic_tab_youtube_off);
        youtubeNavigationItem.setLabel("Youtube");
        navigationItemList.add(youtubeNavigationItem);

        NavigationItem aboutNavigationItem = new NavigationItem(NavigationItem.NavigationItemType.ABOUT);
        aboutNavigationItem.setIcon(R.drawable.ic_tab_about_off);
        aboutNavigationItem.setLabel("About");
        navigationItemList.add(aboutNavigationItem);

        NavigationItem settingsNavigationItem = new NavigationItem(NavigationItem.NavigationItemType.SETTINGS);
//        settingsNavigationItem.setIcon(R.drawable.ic_tab_);
        settingsNavigationItem.setLabel("Settings");
        navigationItemList.add(settingsNavigationItem);

        return navigationItemList;
    }

    public void replaceFragmentToRoot(Fragment fragment) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        replaceFragment(fragment);
    }


    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(NavigationItem navigationChoice) {
        switch (navigationChoice.getNavigationItemType()) {
            case SHOWS:
                getSupportActionBar().setTitle("Shows");
                replaceFragment(ShowsTabFragment.newInstance());
                break;
            case GUESTS:
                getSupportActionBar().setTitle("Guests");
                replaceFragment(GuestsFragment.newInstance());
                break;
            case LIVE:
                getSupportActionBar().setTitle("Live");
                replaceFragment(LiveFragment.newInstance());
                break;
            case SCHEDULE:
                getSupportActionBar().setTitle("Schedule");
                replaceFragment(EventsFragment.newInstance());
                break;
            case YOUTUBE:
                getSupportActionBar().setTitle("Youtube");
                replaceFragment(YoutubeFragment.newInstance());
                break;
            case ABOUT:
                getSupportActionBar().setTitle("About");
                replaceFragment(AboutFragment.newInstance());
                break;
            case SETTINGS:
                getSupportActionBar().setTitle("Settings");
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }
    }

    @Override
    public void onVipButtonClicked() {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
    }

    @Override
    public void onShowSelected(long showId, long episodeId) {
        replaceFragment(EpisodeFragment.newInstance(episodeId));
    }

    @Override
    public void onVisibilityChanged(final boolean visible) {
        //TODO we should be able to animate this visibility change...
        getSupportFragmentManager().beginTransaction().hide(mPlayerFragment).commit();
    }
}
