package com.keithandthegirl.app.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * A bound Service that instantiates the authenticator
 * when started.
 *
 * Created by dmfrey on 3/10/14.
 */
public class AuthenticatorService extends Service {

    private static final String TAG = AuthenticatorService.class.getSimpleName();

    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        Log.v( TAG, "onCreate : enter" );

        // Create a new authenticator object
        mAuthenticator = new Authenticator( this );

        Log.v( TAG, "onCreate : exit" );
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind( Intent intent ) {
        Log.v( TAG, "onBind : enter" );
        Log.v( TAG, "onBind : exit" );
        return mAuthenticator.getIBinder();
    }

}
