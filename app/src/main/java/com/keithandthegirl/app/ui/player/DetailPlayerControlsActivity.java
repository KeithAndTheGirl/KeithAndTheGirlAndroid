package com.keithandthegirl.app.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.services.media.MediaService;
import com.keithandthegirl.app.services.media.MediaService.MediaServiceBinder;
import com.keithandthegirl.app.services.media.MediaService.State;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DetailPlayerControlsActivity extends ActionBarActivity {

    public static final String ARG_SHOW_ID = "arg_show_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int showId = getIntent().getIntExtra(ARG_SHOW_ID, -1);
        setContentView(R.layout.activity_detail_player_controls);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, PlaceholderFragment.newInstance(showId))
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private int mShowId;

        MediaService mService;
        boolean mBound = false;

        @InjectView(R.id.playerSeekBar)
        SeekBar playerSeekBar;

        public static PlaceholderFragment newInstance(int showId) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SHOW_ID, showId);
            fragment.setArguments(args);
            return fragment;
        }   
        
        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mShowId = getArguments().getInt(ARG_SHOW_ID);
            }
        }

        @Override
        public void onStart() {
            super.onStart();

            Intent intent = new Intent(getActivity(), MediaService.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onStop() {
            super.onStop();

            if (mBound) {
                getActivity().unbindService(mConnection);
                mBound = false;
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail_player_controls, container, false);
            ButterKnife.inject(this, rootView);
            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            updateView();
        }

        public void updateView() {

            if( mBound ) {

                if( mService.getState().equals( State.Playing ) || mService.getState().equals( State.Paused ) ) {

                    EpisodeInfoHolder episode = mService.getEpisode();

                    playerSeekBar.setMax( episode.getEpisodeLength() * 1000 );
                    playerSeekBar.setProgress( episode.getEpisodeLastPlayed() );

                }
            }

        }

        private ServiceConnection mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected( ComponentName className, IBinder service ) {

                MediaServiceBinder binder = (MediaServiceBinder) service;
                mService = binder.getService();
                mBound = true;

                updateView();
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }

        };

    }

}
