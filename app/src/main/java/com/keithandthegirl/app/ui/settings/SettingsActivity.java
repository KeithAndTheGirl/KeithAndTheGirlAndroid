package com.keithandthegirl.app.ui.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by dmfrey on 10/17/14.
 */
public class SettingsActivity extends ActionBarActivity {

    public static final String KEY_PREF_SHOW_EXPLICIT = "pref_show_explicit";
    public static final String KEY_PREF_DOWNLOAD_MOBILE = "pref_download_mobile";
    public static final String KEY_PREF_DOWNLOAD_WIFI = "pref_download_wifi";

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled( false );

        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled( true );

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace( android.R.id.content, new SettingsFragment() )
                .commit();
    }

}
