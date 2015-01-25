package com.keithandthegirl.app.sync;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dmfrey on 12/31/14.
 */
public class YoutubeDataFragment extends Fragment implements ShowsLoaderAsyncTask.ShowLoaderListener {

    private static final String TAG = YoutubeDataFragment.class.getSimpleName();

    private boolean loading = false;

    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        Log.v( TAG, "onCreateView : enter" );

        new ShowsLoaderAsyncTask( getActivity(), this ).execute();

        Log.v( TAG, "onCreateView : exit" );
        return null;
    }

    public boolean isLoading() {
        return loading;
    }

    @Override
    public void onStatus( boolean loading ) {
        Log.v( TAG, "onStatus : enter" );

        this.loading = loading;

        Log.v( TAG, "onStatus : exit" );
    }

}
