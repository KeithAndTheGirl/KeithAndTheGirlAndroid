package com.keithandthegirl.app.ui.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by dmfrey on 10/17/14.
 */
public class SettingsActivity extends Activity {

    public static final String KEY_PREF_SHOW_EXPLICIT = "pref_show_explicit";
    public static final String KEY_PREF_DOWNLOAD_MOBILE = "pref_download_mobile";
    public static final String KEY_PREF_DOWNLOAD_WIFI = "pref_download_wifi";

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace( android.R.id.content, new SettingsFragment() )
                .commit();
    }

}
