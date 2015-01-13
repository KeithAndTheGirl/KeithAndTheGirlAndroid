package com.keithandthegirl.app.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.services.media.MediaService;
import com.keithandthegirl.app.services.media.MediaService.MediaServiceBinder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Jeff on 12/16/2014.
 */
public class PlaybackStatusFragment extends Fragment implements MediaService.MediaServiceEventListener {
    private static final String TAG = PlaybackStatusFragment.class.getSimpleName();

    MediaService mMediaService;
    PlayerVisibilityListener mPlayerVisibilityListener = null;
    boolean mBound = false;

    @InjectView(R.id.playerLayout)
    View playerLayout;
    @InjectView(R.id.seekLayout)
    View seekLayout;
    @InjectView(R.id.showImageLayout)
    View showImageLayout;
    @InjectView(R.id.playImageButton)
    ImageButton playImageButton;
    @InjectView(R.id.playbackProgressBar)
    ProgressBar playbackProgressBar;
    @InjectView(R.id.episodeInfoTextView)
    TextView episodeInfoTextView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_katg_player, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(getActivity(), MediaService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateView();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @OnClick({R.id.seekLayout, R.id.showImageLayout})
    public void showTransport(View view) {
        Intent playbackIntent = new Intent(this.getActivity(), PlaybackControlsActivity.class);
        startActivity(playbackIntent);
    }

    public void setPlayerVisibilityListener(PlayerVisibilityListener playerVisibilityListener) {
        mPlayerVisibilityListener = playerVisibilityListener;
    }

    @Override
    public void onStatusUpdate(final EpisodeInfoHolder episodeInfoHolder) {
        updateView();

    }

    private void updateView() {
        if (mBound) {
            switch (mMediaService.getState()) {
                case NONE:
                    if (mPlayerVisibilityListener != null) {
                        mPlayerVisibilityListener.onVisibilityChanged(false);
                    }
                    break;
                case PLAYING:
                case PAUSED:
                    if (mPlayerVisibilityListener != null) {
                        mPlayerVisibilityListener.onVisibilityChanged(true);
                    }
                    EpisodeInfoHolder episode = mMediaService.getEpisode();
                    playbackProgressBar.setMax(episode.getEpisodeLength() * 1000);
                    playbackProgressBar.setProgress(episode.getEpisodeLastPlayed());
                    episodeInfoTextView.setText(episode.getEpisodeTitle());
                    break;
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaServiceBinder binder = (MediaServiceBinder) service;
            mMediaService = binder.getMediaService();
            mMediaService.setMediaServiceEventListener(PlaybackStatusFragment.this);
            mBound = true;

            updateView();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public interface PlayerVisibilityListener {
        void onVisibilityChanged(boolean visible);
    }
}