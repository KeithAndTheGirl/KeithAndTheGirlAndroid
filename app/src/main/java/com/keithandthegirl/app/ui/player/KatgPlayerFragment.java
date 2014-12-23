package com.keithandthegirl.app.ui.player;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.keithandthegirl.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Jeff on 12/16/2014.
 * Copyright Propeller Health 2014
 */
public class KatgPlayerFragment extends Fragment {

    @InjectView(R.id.seekLayout)
    View seekLayout;
    @InjectView(R.id.showImageLayout)
    View showImageLayout;
    @InjectView(R.id.playImageButton)
    ImageButton playImageButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_katg_player, container, false);

        ButterKnife.inject(this, view);
        return view;
    }

    @OnClick( {R.id.seekLayout, R.id.showImageLayout})
    public void showTransport(View view) {
        Intent playbackIntent = new Intent(this.getActivity(), KatgPlaybackActivity.class);
        startActivity(playbackIntent);
    }
}
