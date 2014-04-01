package com.keithandthegirl.app.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created by dmfrey on 4/1/14.
 */
public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

    public static int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight ) {
        Log.v( TAG, "calculateInSampleSize : enter" );

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if( height > reqHeight || width > reqWidth ) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while( ( halfHeight / inSampleSize ) > reqHeight && ( halfWidth / inSampleSize ) > reqWidth ) {
                inSampleSize *= 2;
            }

        }

        Log.v( TAG, "calculateInSampleSize : exit" );
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile( String path, int reqWidth, int reqHeight ) {
        Log.v( TAG, "decodeSampledBitmapFromFile : enter" );

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile( path, options );

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize( options, reqWidth, reqHeight );

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Log.v( TAG, "decodeSampledBitmapFromFile : exit" );
        return BitmapFactory.decodeFile( path, options );
    }

}
