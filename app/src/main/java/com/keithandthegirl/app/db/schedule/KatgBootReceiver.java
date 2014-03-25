package com.keithandthegirl.app.db.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by dmfrey on 3/24/14.
 */
public class KatgBootReceiver extends BroadcastReceiver {

    private static final String TAG = KatgBootReceiver.class.getSimpleName();

    KatgAlarmReceiver alarm = new KatgAlarmReceiver();

    @Override
    public void onReceive( Context context, Intent intent ) {
        Log.i( TAG, "onReceive : enter" );

        if( intent.getAction().equals( "android.intent.action.BOOT_COMPLETED" ) ) {
            Log.i( TAG, "onReceive : setting alarms after device boot" );

            alarm.setAlarm( context );
        }

        Log.i( TAG, "onReceive : exit" );
    }

}
