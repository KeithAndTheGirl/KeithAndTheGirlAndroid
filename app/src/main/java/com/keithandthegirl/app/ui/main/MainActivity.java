package com.keithandthegirl.app.ui.main;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.db.schedule.KatgAlarmReceiver;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.navigationdrawer.NavigationDrawerFragment;
import com.keithandthegirl.app.ui.shows.ShowFragment;

import java.util.ArrayList;

public class MainActivity extends AbstractBaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private static final String TAG = MainActivity.class.getSimpleName();

    KatgAlarmReceiver alarm = new KatgAlarmReceiver();

    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setTitle(R.string.katg_actionbar_title);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment.setupDrawer(R.id.navigation_drawer, drawerLayout);

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
    public void onNavigationDrawerItemSelected(NavigationDrawerFragment.NavigationChoice navigationChoice) {
        switch (navigationChoice) {
            case ABOUT:
                replaceFragment(AboutFragment.newInstance());
                break;
            case LIVE:
                replaceFragment(LiveFragment.newInstance());
                break;
            case SCHEDULE:
                replaceFragment(EventsFragment.newInstance());
                break;
            case YOUTUBE:
                replaceFragment(YoutubeFragment.newInstance());
                break;
            case FEEDBACK:
                getSupportFragmentManager()
                        .beginTransaction()
//                        .replace(R.id.container, Feedback)
                        .commit();
                break;

            case KATG:
                replaceFragment(ShowFragment.newInstance(1));
                break;
            case KATG_TV:
                replaceFragment(ShowFragment.newInstance(3));
                break;
            case WHATS_MY_NAME:
                replaceFragment(ShowFragment.newInstance(5));
                break;
            case MY_NAME_IS_KEITH:
                replaceFragment(ShowFragment.newInstance(7));
                break;
            case THATS_THE_SHOW_WITH_DANNY:
                replaceFragment(ShowFragment.newInstance(9));
                break;
            case BROTHER_LOVE_OWWWR:
                replaceFragment(ShowFragment.newInstance(11));
                break;
            case BOTTOMS_UP:
                replaceFragment(ShowFragment.newInstance(12));
                break;
            case SUPER_HANG:
                replaceFragment(ShowFragment.newInstance(15));
                break;
            case MYKA_FOX_AND_FRIENDS:
                replaceFragment(ShowFragment.newInstance(16));
                break;
            case INTERNMENT:
                replaceFragment(ShowFragment.newInstance(8));
                break;
            case KATG_BEGINNINGS:
                replaceFragment(ShowFragment.newInstance(6));
                break;
            case KATG_LIVE_SHOWS:
                replaceFragment(ShowFragment.newInstance(4));
                break;

        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }


    enum FragmentType {UNSET, SHOWS, LIVE, GUESTS, EVENTS, YOUTUBE, ABOUT}
    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<FragmentHolder> mFragmentList;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            initializeSections();
        }

        private void initializeSections() {
            mFragmentList = new ArrayList<FragmentHolder>();
            mFragmentList.add(new FragmentHolder(FragmentType.SHOWS, R.string.action_bar_tab_shows, R.drawable.ic_tab_shows_on, R.drawable.ic_tab_shows_off));
            mFragmentList.add(new FragmentHolder(FragmentType.LIVE, R.string.action_bar_tab_live, R.drawable.ic_tab_live_on, R.drawable.ic_tab_live_off));
            mFragmentList.add(new FragmentHolder(FragmentType.GUESTS, R.string.action_bar_tab_guests, R.drawable.ic_tab_guest_on, R.drawable.ic_tab_guest_off));
            mFragmentList.add(new FragmentHolder(FragmentType.EVENTS, R.string.action_bar_tab_events, R.drawable.ic_tab_calendar_on, R.drawable.ic_tab_calendar_off));
            mFragmentList.add(new FragmentHolder(FragmentType.YOUTUBE, R.string.action_bar_tab_youtube, R.drawable.ic_tab_youtube_on, R.drawable.ic_tab_youtube_off));
            mFragmentList.add(new FragmentHolder(FragmentType.ABOUT, R.string.action_bar_tab_about, R.drawable.ic_tab_about_on, R.drawable.ic_tab_about_off));
        }

        @Override
        public Fragment getItem(int position) {
            FragmentHolder holder = mFragmentList.get(position);
            switch (holder.getFragmentType()) {
                case SHOWS:
                    return ShowsGridFragment.newInstance();
                case LIVE:
                    return LiveFragment.newInstance();
                case GUESTS:
                    return GuestsFragment.newInstance();
                case EVENTS:
                    return EventsFragment.newInstance();
                case YOUTUBE:
                    return YoutubeFragment.newInstance();
                case ABOUT:
                    return AboutFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getString(mFragmentList.get(position).getTitleStringId());
        }

        public Drawable getUnselectedIcon(int position) {
            return getResources().getDrawable(mFragmentList.get(position).getUnselectedIconId());
        }

        public Drawable getSelectedIcon(int position) {
            return getResources().getDrawable(mFragmentList.get(position).getSelectedIconId());
        }
    }

    private class FragmentHolder {
        private FragmentType mFragmentType;
        private int mTitleStringId;
        private int mSelectedIconId;
        private int mUnselectedIconId;

        public FragmentHolder(final FragmentType fragmentType, final int titleStringId, final int selectedIconId, final int unselectedIconId) {
            mFragmentType = fragmentType;
            mTitleStringId = titleStringId;
            mSelectedIconId = selectedIconId;
            mUnselectedIconId = unselectedIconId;
        }

        public FragmentType getFragmentType() {
            return mFragmentType;
        }

        public void setFragmentType(final FragmentType fragmentType) {
            mFragmentType = fragmentType;
        }

        public int getTitleStringId() {
            return mTitleStringId;
        }

        public void setTitleStringId(final int titleStringId) {
            mTitleStringId = titleStringId;
        }

        public int getSelectedIconId() {
            return mSelectedIconId;
        }

        public void setSelectedIconId(final int selectedIconId) {
            mSelectedIconId = selectedIconId;
        }

        public int getUnselectedIconId() {
            return mUnselectedIconId;
        }

        public void setUnselectedIconId(final int unselectedIconId) {
            mUnselectedIconId = unselectedIconId;
        }
    }
}
