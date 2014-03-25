package com.keithandthegirl.app.db.schedule;

import android.accounts.Account;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.keithandthegirl.app.MainApplication;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.WorkItem;

/**
 * Created by dmfrey on 3/24/14.
 */
public class KatgSchedulingService extends IntentService {

    private static final String TAG = KatgSchedulingService.class.getSimpleName();

    public KatgSchedulingService() {
        super( "KatgSchedulingService" );
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        Log.i( TAG, "onHandleIntent : enter" );

        Account mAccount = MainApplication.CreateSyncAccount( this );

        ContentResolver.setSyncAutomatically( mAccount, KatgProvider.AUTHORITY, true );

        WorkItem.Type type = WorkItem.Type.valueOf( intent.getStringExtra( WorkItem.FIELD_FREQUENCY ) );
        Log.i( TAG, "onHandleIntent : executing sync service for '" + type.name() + "' jobs" );

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_MANUAL, true );
        settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_EXPEDITED, true );
        settingsBundle.putString( WorkItem.FIELD_FREQUENCY, type.name() );

        ContentResolver.requestSync( mAccount, KatgProvider.AUTHORITY, settingsBundle );

        Log.i( TAG, "onHandleIntent : exit" );
    }

}
