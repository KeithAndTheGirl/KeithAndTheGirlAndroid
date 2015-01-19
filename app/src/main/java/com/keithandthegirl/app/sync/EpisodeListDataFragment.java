package com.keithandthegirl.app.sync;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dmfrey on 1/18/15.
 */
public class EpisodeListDataFragment extends Fragment {

    private static final String TAG = EpisodeListDataFragment.class.getSimpleName();

    public static final String SHOW_NAME_ID_KEY = "showNameId";

    private boolean loading = false;

    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        Log.v( TAG, "onCreateView : enter" );

        long showNameId = 1;

        Bundle arguments = getArguments();
        if( arguments.containsKey( SHOW_NAME_ID_KEY ) ) {
            showNameId = arguments.getLong( SHOW_NAME_ID_KEY );
        }

        new EpisodeListAsyncTask( getActivity(), (int) showNameId, -1, -1, 50, true ).execute();

        Log.v( TAG, "onCreateView : exit" );
        return null;
    }

}
