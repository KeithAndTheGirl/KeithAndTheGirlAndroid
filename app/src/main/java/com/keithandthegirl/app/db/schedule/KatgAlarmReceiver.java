package com.keithandthegirl.app.db.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.keithandthegirl.app.db.model.WorkItem;

import java.util.Calendar;

/**
 * Created by dmfrey on 3/24/14.
 */
public class KatgAlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = KatgAlarmReceiver.class.getSimpleName();

    private AlarmManager mAlarmManager;
    private PendingIntent mHourlyAlarmIntent, mDailyAlarmIntent, mWeeklyAlarmIntent;

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

        Intent hourlyIntent = new Intent( context, KatgAlarmReceiver.class );
        hourlyIntent.putExtra( WorkItem.FIELD_FREQUENCY, WorkItem.Type.HOURLY.name() );
        mHourlyAlarmIntent = PendingIntent.getBroadcast( context, 0, hourlyIntent, 0 );

        Calendar hourly = Calendar.getInstance();
        hourly.setTimeInMillis( System.currentTimeMillis() );
//        hourly.set( Calendar.MINUTE, hourly.get( Calendar.MINUTE ) + 2 );

        mAlarmManager.setInexactRepeating( AlarmManager.ELAPSED_REALTIME, hourly.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, mHourlyAlarmIntent );
        Log.v( TAG, "setAlarm : hourly alarm set!" );

        Intent dailyIntent = new Intent( context, KatgAlarmReceiver.class );
        hourlyIntent.putExtra( WorkItem.FIELD_FREQUENCY, WorkItem.Type.DAILY.name() );
        mDailyAlarmIntent = PendingIntent.getBroadcast( context, 0, dailyIntent, 0 );

        Calendar daily = Calendar.getInstance();
        daily.setTimeInMillis( System.currentTimeMillis() );
        daily.set( Calendar.HOUR_OF_DAY, 0 );
        daily.set( Calendar.MINUTE, 0 );

        mAlarmManager.setInexactRepeating( AlarmManager.ELAPSED_REALTIME, daily.getTimeInMillis(), AlarmManager.INTERVAL_DAY, mDailyAlarmIntent );
        Log.v( TAG, "setAlarm : daily alarm set!" );

        Intent weeklyIntent = new Intent( context, KatgAlarmReceiver.class );
        hourlyIntent.putExtra( WorkItem.FIELD_FREQUENCY, WorkItem.Type.WEEKLY.name() );
        mWeeklyAlarmIntent = PendingIntent.getBroadcast( context, 0, weeklyIntent, 0 );

        Calendar weekly = Calendar.getInstance();
        weekly.setTimeInMillis( System.currentTimeMillis() );
        weekly.set( Calendar.HOUR_OF_DAY, 0 );
        weekly.set( Calendar.MINUTE, 0 );
        weekly.set( Calendar.DAY_OF_WEEK, 0 );

        mAlarmManager.setInexactRepeating( AlarmManager.ELAPSED_REALTIME, weekly.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, mWeeklyAlarmIntent );
        Log.v( TAG, "setAlarm : weekly alarm set!" );

        ComponentName receiver = new ComponentName( context, KatgBootReceiver.class );
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting( receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP );

        Log.d( TAG, "setAlarm : exit" );
    }

    public void cancelAlarm( Context context ) {
        Log.d( TAG, "cancelAlarm : enter" );

        if( null != mAlarmManager ) {
            mAlarmManager.cancel( mHourlyAlarmIntent );
            mAlarmManager.cancel( mDailyAlarmIntent );
            mAlarmManager.cancel( mWeeklyAlarmIntent );
        }

        ComponentName receiver = new ComponentName( context, KatgBootReceiver.class );
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting( receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP );

        Log.d( TAG, "cancelAlarm : exit" );
    }

}
