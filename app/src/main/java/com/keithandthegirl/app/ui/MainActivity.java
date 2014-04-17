package com.keithandthegirl.app.ui;

import android.accounts.Account;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.keithandthegirl.app.MainApplication;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.db.schedule.KatgAlarmReceiver;
import com.keithandthegirl.app.ui.events.EventsFragment;
import com.keithandthegirl.app.ui.guests.GuestsFragment;
import com.keithandthegirl.app.ui.shows.ShowsGridFragment;

public class MainActivity extends AbstractBaseActivity implements ActionBar.TabListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    KatgAlarmReceiver alarm = new KatgAlarmReceiver();
    Account mAccount;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        Log.d( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_main );

        mAccount = MainApplication.CreateSyncAccount( this );

        boolean neverRun = false;
        Cursor cursor = getContentResolver().query( Show.CONTENT_URI, null, null, null, null );
        if( cursor.getCount() == 0 ) {
            neverRun = true;
        }
        cursor.close();

        ContentResolver.setSyncAutomatically( mAccount, KatgProvider.AUTHORITY, true );

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
            actionBar.addTab(
                    actionBar.newTab()
                            .setText( mSectionsPagerAdapter.getPageTitle( i ) )
                            .setTabListener( this ) );

        }

        Log.d( TAG, "onCreate : exit" );
    }

    @Override
    public void onTabSelected( ActionBar.Tab tab, FragmentTransaction fragmentTransaction ) {

        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem( tab.getPosition() );

    }

    @Override
    public void onTabUnselected( ActionBar.Tab tab, FragmentTransaction fragmentTransaction ) {



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
            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
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
            }

            return null;
        }

    }

}
