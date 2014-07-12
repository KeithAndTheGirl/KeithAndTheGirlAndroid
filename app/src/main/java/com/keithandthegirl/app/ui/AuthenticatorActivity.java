package com.keithandthegirl.app.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.account.AccountGeneral;

import java.util.StringTokenizer;

import static com.keithandthegirl.app.account.AccountGeneral.sServerAuthenticate;

/**
 * The Authenticator activity.
 *
 * Called by the Authenticator and in charge of identifing the user.
 *
 * It sends back to the Authenticator the result.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private final String TAG = AuthenticatorActivity.class.getSimpleName();

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;

    private AccountManager mAccountManager;
    private String mAuthTokenType;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.d( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        setContentView( R.layout.act_login );

        mAccountManager = AccountManager.get( getBaseContext() );

        String accountName = getIntent().getStringExtra( ARG_ACCOUNT_NAME );
        mAuthTokenType = getIntent().getStringExtra( ARG_AUTH_TYPE );
        if( mAuthTokenType == null ) {
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;
        }

        if( accountName != null ) {
            ( (TextView) findViewById( R.id.accountName ) ).setText( accountName );
        }

        findViewById( R.id.submit ).setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                Log.d( TAG, "submit.onClick : enter" );

                submit();

                Log.d( TAG, "submit.onClick : exit" );
            }

        });

        Log.d( TAG, "onCreate : exit" );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        Log.d( TAG, "onActivityResult : enter" );

        // The sign up activity returned that the user has successfully created an account
        if( requestCode == REQ_SIGNUP && resultCode == RESULT_OK ) {
            finishLogin( data );
        } else {
            super.onActivityResult( requestCode, resultCode, data );
        }

        Log.d( TAG, "onActivityResult : exit" );
    }

    public void submit() {
        Log.d( TAG, "submit : enter" );

        final String userName = ((TextView) findViewById( R.id.accountName ) ).getText().toString();
        final String userPass = ((TextView) findViewById( R.id.accountPassword ) ).getText().toString();

        final String accountType = getIntent().getStringExtra( ARG_ACCOUNT_TYPE );

        new AsyncTask<String, Void, Intent>() {

            @Override
            protected Intent doInBackground( String... params ) {
                Log.d( TAG, "submit.doInBackground : enter" );

                String authtoken = null;
                Bundle data = new Bundle();
                try {
                    authtoken = sServerAuthenticate.userSignIn( userName, userPass, mAuthTokenType );

                    data.putString( AccountManager.KEY_ACCOUNT_NAME, userName );
                    data.putString( AccountManager.KEY_ACCOUNT_TYPE, accountType );
                    data.putString( AccountManager.KEY_AUTHTOKEN, authtoken );
                    data.putString( PARAM_USER_PASS, userPass );

                } catch( Exception e ) {
                    Log.e( TAG, "submit.doInBackground : error", e );

                    data.putString( KEY_ERROR_MESSAGE, e.getMessage() );
                }

                final Intent res = new Intent();
                res.putExtras( data );

                Log.d( TAG, "submit.doInBackground : exit" );
                return res;
            }

            @Override
            protected void onPostExecute( Intent intent ) {
                Log.d( TAG, "submit.onPostExecute : enter" );

                if( intent.hasExtra( KEY_ERROR_MESSAGE ) ) {

                    Toast.makeText( getBaseContext(), intent.getStringExtra( KEY_ERROR_MESSAGE ), Toast.LENGTH_SHORT ).show();

                } else {

                    finishLogin( intent );

                }

                Log.d( TAG, "submit.onPostExecute : exit" );
            }

        }.execute();

        Log.d( TAG, "submit : exit" );
    }

    private void finishLogin( Intent intent ) {
        Log.d( TAG,  "finishLogin : enter" );

        Account dummyAccount = null;

        Account[] accounts = mAccountManager.getAccountsByType( AccountGeneral.ACCOUNT_TYPE );
        Log.d( TAG,  "finishLogin : accounts.length = " + accounts.length );
        if( accounts.length == 1 ) {
            dummyAccount = accounts[ 0 ];
        }

        if( null != dummyAccount ) {
            Log.d( TAG,  "finishLogin : found dummy account" );

            final Handler handler = new Handler();

            AccountManagerCallback<Boolean> callback = new AccountManagerCallback<Boolean>() {

                @Override
                public void run(AccountManagerFuture<Boolean> future) {
                    String test = "test";
                }

            };

            mAccountManager.removeAccount( AccountGeneral.dummyAccount(), callback, handler );
            Log.d( TAG,  "finishLogin : removed dummy account" );
        }

        String accountName = intent.getStringExtra( AccountManager.KEY_ACCOUNT_NAME );
//        Log.d( TAG,  "finishLogin : accountName=" + accountName );

        String accountPassword = intent.getStringExtra( PARAM_USER_PASS );
//        Log.d( TAG,  "finishLogin : accountPassword=" + accountPassword );

        final Account account = new Account( accountName, AccountGeneral.ACCOUNT_TYPE );

//        if( getIntent().getBooleanExtra( ARG_IS_ADDING_NEW_ACCOUNT, false ) ) {
            Log.d( TAG, "finishLogin : addAccountExplicitly" );

            String authtoken = intent.getStringExtra( AccountManager.KEY_AUTHTOKEN );
            String authtokenType = mAuthTokenType;

            StringTokenizer st = new StringTokenizer( authtoken, "|" );
            String katgVipUid = st.nextToken();
            String katgVipKey = st.nextToken();

            Bundle userData = new Bundle();
            userData.putString( AccountGeneral.KATG_VIP_UID, katgVipUid );
            userData.putString( AccountGeneral.KATG_VIP_KEY, katgVipKey );

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly( account, accountPassword, userData );
            mAccountManager.setAuthToken( account, authtokenType, authtoken );

//        } else {
//            Log.d( TAG, "finishLogin : setPassword" );
//            mAccountManager.setPassword( account, accountPassword );
//        }

        setAccountAuthenticatorResult( intent.getExtras() );
        setResult( RESULT_OK, intent );

        Log.d( TAG,  "finishLogin : exit" );
        finish();
    }

}
