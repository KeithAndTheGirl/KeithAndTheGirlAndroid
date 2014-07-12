package com.keithandthegirl.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.keithandthegirl.app.account.AccountGeneral;

/**
 * Created by dmfrey on 3/19/14.
 */
public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();

    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "keithandthegirl.com";

    // The account name
    public static final String ACCOUNT = "dummyaccount";

    @Override
    public void onCreate() {
        Log.v( TAG, "onCreate : enter" );
        super.onCreate();

        Log.v( TAG, "onCreate : exit" );
    }

    public static Account CreateSyncAccount( Context context ) {
        Log.v( TAG, "CreateSyncAccount : enter" );

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

            Log.v( TAG, "CreateSyncAccount : exit" );
            return newAccount;
        }

        Log.v( TAG, "CreateSyncAccount : exit, returning existing account" );
        return accounts[ 0 ];
    }

}
