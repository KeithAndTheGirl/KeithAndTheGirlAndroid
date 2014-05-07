package com.keithandthegirl.app.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.db.schedule.KatgAlarmReceiver;
import com.keithandthegirl.app.ui.about.AboutFragment;
import com.keithandthegirl.app.ui.events.EventsFragment;
import com.keithandthegirl.app.ui.guests.GuestsFragment;
import com.keithandthegirl.app.ui.shows.ShowsGridFragment;
import com.keithandthegirl.app.ui.youtube.YoutubeFragment;

public class MainActivity extends AbstractBaseActivity implements ActionBar.TabListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    KatgAlarmReceiver alarm = new KatgAlarmReceiver();

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        Log.d( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_main );

        boolean neverRun = false;
        Cursor cursor = getContentResolver().query( Show.CONTENT_URI, null, null, null, null );
        if( cursor.getCount() == 0 ) {
            neverRun = true;
        }
        cursor.close();

        if( neverRun ) {

            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_MANUAL, true );
            settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_EXPEDITED, true );

            Log.v( TAG, "onCreate : requesting sync" );
            ContentResolver.requestSync( mAccount, KatgProvider.AUTHORITY, settingsBundle );
        }

        alarm.setAlarm( this );

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_TABS );

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter( getSupportFragmentManager() );

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById( R.id.pager );
        mViewPager.setAdapter( mSectionsPagerAdapter );

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener( new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected( int position ) {
                actionBar.setSelectedNavigationItem( position );
            }

        });

        // For each of the sections in the app, add a tab to the action bar.
        for( int i = 0; i < mSectionsPagerAdapter.getCount(); i++ ) {

            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.custom_tab, null);

            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            icon.setImageDrawable(mSectionsPagerAdapter.getUnselectedIcon(i));

            TextView title = (TextView) view.findViewById( R.id.title );
            title.setText( mSectionsPagerAdapter.getPageTitle( i ) );

            actionBar.addTab( actionBar.newTab()
                    .setCustomView(view)
                    .setTabListener( this )
            );

            //            actionBar.addTab(
//                    actionBar.newTab()
//                            .setText( mSectionsPagerAdapter.getPageTitle( i ) )
//                            .setIcon( mSectionsPagerAdapter.getUnselectedIcon( i ) )
//                            .setTabListener( this )
//            );

        }

        Log.d( TAG, "onCreate : exit" );
    }

    @Override
    public void onTabSelected( ActionBar.Tab tab, FragmentTransaction fragmentTransaction ) {

        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem( tab.getPosition() );

        ImageView icon = (ImageView) tab.getCustomView().findViewById( R.id.icon );
        icon.setImageDrawable( mSectionsPagerAdapter.getSelectedIcon(tab.getPosition()) );

        TextView title = (TextView) tab.getCustomView().findViewById(R.id.title);
        title.setTextColor(Color.WHITE);

//        tab.setIcon( mSectionsPagerAdapter.getSelectedIcon( tab.getPosition() ) );

    }

    @Override
    public void onTabUnselected( ActionBar.Tab tab, FragmentTransaction fragmentTransaction ) {

        ImageView icon = (ImageView) tab.getCustomView().findViewById( R.id.icon );
        icon.setImageDrawable( mSectionsPagerAdapter.getUnselectedIcon(tab.getPosition()) );

        TextView title = (TextView) tab.getCustomView().findViewById(R.id.title);
        title.setTextColor(Color.BLACK);

//        tab.setIcon( mSectionsPagerAdapter.getUnselectedIcon(tab.getPosition()) );

    }

    @Override
    public void onTabReselected( ActionBar.Tab tab, FragmentTransaction fragmentTransaction ) {
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter( FragmentManager fm ) {

            super( fm );

        }

        @Override
        public Fragment getItem( int position ) {

            switch ( position ) {
                case 0:
                    return new ShowsGridFragment();
                case 1:
                    return new GuestsFragment();
                case 2:
                    return new EventsFragment();
                case 3:
                    return new YoutubeFragment();
                case 4:
                    return new AboutFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle( int position ) {

            switch( position ) {
                case 0:
                    return getResources().getString( R.string.action_bar_tab_shows );
                case 1:
                    return getResources().getString( R.string.action_bar_tab_guests );
                case 2:
                    return getResources().getString( R.string.action_bar_tab_events );
                case 3:
                    return getResources().getString( R.string.action_bar_tab_youtube );
                case 4:
                    return getResources().getString( R.string.action_bar_tab_about );
            }

            return null;
        }

        public Drawable getUnselectedIcon( int position ) {

            switch( position ) {
                case 0:
                    return getResources().getDrawable( R.drawable.ic_tab_shows_off );
                case 1:
                    return null;
                case 2:
                    return getResources().getDrawable( R.drawable.ic_tab_calendar_off );
                case 3:
                    return getResources().getDrawable( R.drawable.ic_tab_youtube_off );
                case 4:
                    return getResources().getDrawable( R.drawable.ic_tab_about_off );
            }

            return null;
        }

        public Drawable getSelectedIcon( int position ) {

            switch( position ) {
                case 0:
                    return getResources().getDrawable( R.drawable.ic_tab_shows_on );
                case 1:
                    return null;
                case 2:
                    return getResources().getDrawable( R.drawable.ic_tab_calendar_on );
                case 3:
                    return getResources().getDrawable( R.drawable.ic_tab_youtube_on );
                case 4:
                    return getResources().getDrawable( R.drawable.ic_tab_about_on );
            }

            return null;
        }

    }

}
