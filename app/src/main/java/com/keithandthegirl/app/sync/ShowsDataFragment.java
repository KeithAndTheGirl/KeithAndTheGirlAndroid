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
public class ShowsDataFragment extends Fragment {

    private static final String TAG = ShowsDataFragment.class.getSimpleName();

    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        Log.v( TAG, "onCreateView : enter" );

        new ShowsLoaderAsyncTask( getActivity() ).execute();

        Log.v( TAG, "onCreateView : exit" );
        return null;
    }

}
