package com.keithandthegirl.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by dmfrey on 7/12/15.
 */
public class NetworkUtils {

    public static boolean isOnline( Context context ) {

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return ( networkInfo != null && networkInfo.isConnected() );
    }

}