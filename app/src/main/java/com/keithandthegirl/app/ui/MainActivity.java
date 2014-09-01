package com.keithandthegirl.app.ui;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.db.schedule.KatgAlarmReceiver;
import com.keithandthegirl.app.ui.about.AboutFragment;
import com.keithandthegirl.app.ui.events.EventsFragment;
import com.keithandthegirl.app.ui.guests.GuestsFragment;
import com.keithandthegirl.app.ui.live.LiveFragment;
import com.keithandthegirl.app.ui.shows.ShowsGridFragment;
import com.keithandthegirl.app.ui.youtube.YoutubeFragment;

import java.util.ArrayList;

public class MainActivity extends AbstractBaseActivity implements ActionBar.TabListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    KatgAlarmReceiver alarm = new KatgAlarmReceiver();

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

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

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {

            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.custom_tab, null, false);

            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            icon.setImageDrawable(mSectionsPagerAdapter.getUnselectedIcon(i));

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(mSectionsPagerAdapter.getPageTitle(i));

            actionBar.addTab(actionBar.newTab()
                            .setCustomView(view)
                            .setTabListener(this)
            );

            //            actionBar.addTab(
//                    actionBar.newTab()
//                            .setText( mSectionsPagerAdapter.getPageTitle( i ) )
//                            .setIcon( mSectionsPagerAdapter.getUnselectedIcon( i ) )
//                            .setTabListener( this )
//            );

        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

        ImageView icon = (ImageView) tab.getCustomView().findViewById(R.id.icon);
        icon.setImageDrawable(mSectionsPagerAdapter.getSelectedIcon(tab.getPosition()));

        TextView title = (TextView) tab.getCustomView().findViewById(R.id.title);
        title.setTextColor(getResources().getColor(R.color.katg_tab_text_selected_color));
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        ImageView icon = (ImageView) tab.getCustomView().findViewById(R.id.icon);
        icon.setImageDrawable(mSectionsPagerAdapter.getUnselectedIcon(tab.getPosition()));

        TextView title = (TextView) tab.getCustomView().findViewById(R.id.title);
        title.setTextColor(getResources().getColor(R.color.katg_tab_text_unselected_color));

//        tab.setIcon( mSectionsPagerAdapter.getUnselectedIcon(tab.getPosition()) );

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
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
            switch (holder.mFragmentType) {
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
