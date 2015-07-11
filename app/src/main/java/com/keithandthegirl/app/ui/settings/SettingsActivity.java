package com.keithandthegirl.app.ui.settings;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.ui.AbstractBaseActivity;

/**
 * Created by dmfrey on 10/17/14.
 */
public class SettingsActivity extends AbstractBaseActivity {

    public static final String KEY_PREF_SHOW_EXPLICIT = "pref_show_explicit";
    public static final String KEY_PREF_DOWNLOAD_MOBILE = "pref_download_mobile";
    public static final String KEY_PREF_DOWNLOAD_WIFI = "pref_download_wifi";

    @Override
    protected int getLayoutResource() {

        return R.layout.activity_settings;
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        getSupportActionBar().setTitle(getResources().getString(R.string.menu_item_settings));

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace( R.id.container, new SettingsFragment() )
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0 ) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

}
