package com.keithandthegirl.app.sync;

import android.content.ContentUris;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.keithandthegirl.app.db.model.Live;
import com.keithandthegirl.app.db.model.LiveConstants;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by dmfrey on 12/31/14.
 */
public class BroadcastingDataFragment extends Fragment {

    private static final String TAG = BroadcastingDataFragment.class.getSimpleName();

    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        Log.v( TAG, "onCreateView : enter" );

        new BroadcastingLoaderAsyncTask( getActivity() ).execute();

        Log.v( TAG, "onCreateView : exit" );
        return null;
    }

}
