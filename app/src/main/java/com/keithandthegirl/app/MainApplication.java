package com.keithandthegirl.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.keithandthegirl.app.ui.settings.SettingsActivity;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;

/**
 * Created by dmfrey on 3/19/14.
 */
public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();

    private static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();

        MainApplication.sAppContext = getApplicationContext();
        if (BuildConfig.SEND_CRASHLYTICS) {
            Fabric.with( this, new Crashlytics() );
        }

        if (BuildConfig.DEBUG) {
            Picasso.with(this).setIndicatorsEnabled(false);
        }

    }

    public static boolean isExplicitAllowed() {
        PreferenceManager.setDefaultValues(sAppContext, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(sAppContext);
        boolean isExplicitAllowed = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_SHOW_EXPLICIT, false);
        return isExplicitAllowed;
    }

}
