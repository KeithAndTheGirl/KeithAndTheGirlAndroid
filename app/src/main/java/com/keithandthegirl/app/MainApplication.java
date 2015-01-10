package com.keithandthegirl.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.keithandthegirl.app.account.AccountGeneral;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

/**
 * Created by dmfrey on 3/19/14.
 */
public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();

    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "keithandthegirl.com";

    // The account name
    public static final String ACCOUNT = "dummyaccount";

    private AlarmManager mAlarmManager;
    private PendingIntent mBroadcastingPendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
//        Crashlytics.start(this);
        if (BuildConfig.DEBUG) {
            Picasso.with(this).setIndicatorsEnabled(false);
        }

//        mAlarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
//        Intent intent = new Intent( this, AlarmReceiver.class );
//        mBroadcastingPendingIntent = PendingIntent.getBroadcast( this, 0, intent, 0 );
//
//        mAlarmManager.setInexactRepeating( AlarmManager.RTC_WAKEUP, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_HOUR, mBroadcastingPendingIntent );
    }

    public static Account CreateSyncAccount( Context context ) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService( ACCOUNT_SERVICE );
        Account[] accounts = accountManager.getAccountsByType( AccountGeneral.ACCOUNT_TYPE );
        if( accounts.length == 0 ) {

            // Create the account type and default account
            Account newAccount = AccountGeneral.dummyAccount();

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if( accountManager.addAccountExplicitly( newAccount, null, null ) ) {
                Log.v( TAG, "CreateSyncAccount : account added explicitly" );

            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            } else {
                Log.v( TAG, "CreateSyncAccount : account already exists" );

            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */

            }

            return newAccount;
        }

        return accounts[ 0 ];
    }
}
