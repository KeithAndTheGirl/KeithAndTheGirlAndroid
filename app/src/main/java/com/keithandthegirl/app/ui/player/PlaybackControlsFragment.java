package com.keithandthegirl.app.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.services.media.MediaService;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PlaybackControlsFragment extends Fragment implements MediaService.MediaServiceEventListener {
    private static final String TAG = PlaybackControlsFragment.class.getSimpleName();

    private int mShowId;

    MediaService mMediaService;
    boolean mBound = false;

    @InjectView(R.id.playerSeekBar)
    SeekBar playerSeekBar;

    public static PlaybackControlsFragment newInstance(int showId) {
        PlaybackControlsFragment fragment = new PlaybackControlsFragment();
        Bundle args = new Bundle();
        args.putInt(PlaybackControlsActivity.ARG_SHOW_ID, showId);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaybackControlsFragment() {
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mShowId = getArguments().getInt(PlaybackControlsActivity.ARG_SHOW_ID);
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

    @Override
    public void onStatusUpdate(final EpisodeInfoHolder episodeInfoHolder) {

    }

    private void updateView() {
        if( mBound ) {
            if( mMediaService.getState().equals( MediaService.State.PLAYING) || mMediaService.getState().equals( MediaService.State.PAUSED) ) {
                EpisodeInfoHolder episode = mMediaService.getEpisode();

                playerSeekBar.setMax( episode.getEpisodeLength() * 1000 );
                playerSeekBar.setProgress( episode.getEpisodeLastPlayed() );
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected( ComponentName className, IBinder service ) {

            MediaService.MediaServiceBinder binder = (MediaService.MediaServiceBinder) service;
            mMediaService = binder.getMediaService();
            mMediaService.setMediaServiceEventListener(PlaybackControlsFragment.this);
            mBound = true;
            updateView();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
