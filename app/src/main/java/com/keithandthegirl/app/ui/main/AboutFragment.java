package com.keithandthegirl.app.ui.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.keithandthegirl.app.R;

/**
 * Created by dmfrey on 4/17/14.
 */
public class AboutFragment extends Fragment {
    private static final String TAG = AboutFragment.class.getSimpleName();

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    public AboutFragment() { }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_about, container, false );
        Button moreChemdaButton = (Button) view.findViewById(R.id.moreChemdaButton);
        Button moreKeithButton = (Button) view.findViewById(R.id.moreKeithButton);
        Button getTheGuideButton = (Button) view.findViewById(R.id.getTheGuideButton);
        return view;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );
    }
}
