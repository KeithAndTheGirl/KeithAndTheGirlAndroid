package com.keithandthegirl.app.ui.youtube;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keithandthegirl.app.R;

/**
 * Created by dmfrey on 4/17/14.
 */
public class YoutubeFragment extends Fragment {

    public static YoutubeFragment newInstance() {

        YoutubeFragment fragment = new YoutubeFragment();

        return fragment;
    }

    public YoutubeFragment() { }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        // Inflate the layout for this fragment

        return inflater.inflate( R.layout.fragment_youtube, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );

    }

}
