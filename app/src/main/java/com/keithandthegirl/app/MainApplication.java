package com.keithandthegirl.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.keithandthegirl.app.ui.settings.SettingsActivity;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Created by dmfrey on 3/19/14.
 */
public class MainApplication extends Application {
    private static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();

        MainApplication.sAppContext = getApplicationContext();
        if (BuildConfig.SEND_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }

        if (BuildConfig.DEBUG) {
            Picasso.with(this).setIndicatorsEnabled(false);
        }

        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashlyticsTree());
        }

        LeakCanary.install(this);
    }

    public static boolean isExplicitAllowed() {
        PreferenceManager.setDefaultValues(sAppContext, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(sAppContext);
        boolean isExplicitAllowed = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_SHOW_EXPLICIT, false);
        return isExplicitAllowed;
    }

    private class CrashlyticsTree extends Timber.Tree {
        @Override
        protected boolean isLoggable(int priority) {
            switch (priority) {
                case Log.ERROR:
                case Log.WARN:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (t != null) {
                Crashlytics.getInstance().core.logException(t);
            } else {
                Crashlytics.getInstance().core.log(priority, tag, message);
            }
        }
    }
}
