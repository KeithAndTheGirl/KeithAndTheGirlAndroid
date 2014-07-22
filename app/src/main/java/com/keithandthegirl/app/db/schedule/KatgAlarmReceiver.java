package com.keithandthegirl.app.db.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by dmfrey on 3/24/14.
 */
public class KatgAlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = KatgAlarmReceiver.class.getSimpleName();

    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmIntent;

    @Override
    public void onReceive( Context context, Intent intent ) {
        Log.i( TAG, "onReceive : enter" );

        Intent service = new Intent( context, KatgSchedulingService.class );

        startWakefulService( context, service );

        Log.i( TAG, "onReceive : exit" );
    }

    public void setAlarm( Context context ) {
        Log.d( TAG, "setAlarm : enter" );

        mAlarmManager = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );

        Intent intent = new Intent( context, KatgAlarmReceiver.class );
        mAlarmIntent = PendingIntent.getBroadcast( context, 0, intent, 0 );

        mAlarmManager.setInexactRepeating( AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_HALF_HOUR, AlarmManager.INTERVAL_HALF_HOUR, mAlarmIntent );
        Log.v( TAG, "setAlarm : alarm set!" );

        ComponentName receiver = new ComponentName( context, KatgBootReceiver.class );
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting( receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP );

        Log.d( TAG, "setAlarm : exit" );
    }

    public void cancelAlarm( Context context ) {
        Log.d( TAG, "cancelAlarm : enter" );

        if( null != mAlarmManager ) {
            mAlarmManager.cancel( mAlarmIntent );
        }

        ComponentName receiver = new ComponentName( context, KatgBootReceiver.class );
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting( receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP );

        Log.d( TAG, "cancelAlarm : exit" );
    }

}
