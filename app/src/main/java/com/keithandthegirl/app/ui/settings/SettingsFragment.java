package com.keithandthegirl.app.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.keithandthegirl.app.R;

/**
 * Created by dmfrey on 10/17/14.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // Load the preferences from an XML resource
        addPreferencesFromResource( R.xml.preferences );

    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }
}
