package com.keithandthegirl.app.sync;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by dmfrey on 1/19/15.
 */
public class DeviceBootReceiver extends BroadcastReceiver {

    private static final String TAG = DeviceBootReceiver.class.getSimpleName();

    @Override
    public void onReceive( Context context, Intent intent ) {
        Log.v( TAG, "onReceive : enter" );

        if( intent.getAction().equals( "android.intent.action.BOOT_COMPLETED" ) ) {
            Log.v( TAG, "onReceive : BOOT_COMPLETED received" );

            Intent alarmIntent = new Intent( context, KatgAlarmReceiver.class );

            PendingIntent pendingIntent = PendingIntent.getBroadcast( context, 0, alarmIntent, 0 );

            AlarmManager manager = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );

            manager.setInexactRepeating( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent );

        }

        Log.v( TAG, "onReceive : exit" );
    }

}
