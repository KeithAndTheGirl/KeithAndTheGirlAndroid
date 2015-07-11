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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.services.media.MediaService;
import com.keithandthegirl.app.services.media.MediaService.MediaServiceBinder;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jeff on 12/16/2014.
 */
public class PlaybackStatusFragment extends Fragment implements MediaService.MediaServiceEventListener {
    private static final String TAG = PlaybackStatusFragment.class.getSimpleName();
    private static final String EPISODE_INFO_HOLDER = "EPISODE_INFO_HOLDER";
    private static final String PLAYER_VISIBILITY = "PLAYER_VISIBILITY";

    MediaService mMediaService;
    boolean mBound = false;

    @Bind(R.id.seekLayout)
    View mSeekLayout;
    @Bind(R.id.showImageLayout)
    View mShowImageLayout;
    @Bind(R.id.playImageButton)
    ImageButton mPlayImageButton;
    @Bind(R.id.playbackProgressBar)
    ProgressBar mPlaybackProgressBar;
    @Bind(R.id.episodeInfoTextView)
    TextView mEpisodeInfoTextView;

    private EpisodeInfoHolder mEpisodeInfoHolder;
    private boolean mIsVisible;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mEpisodeInfoHolder = savedInstanceState.getParcelable(EPISODE_INFO_HOLDER);
            mIsVisible = savedInstanceState.getBoolean(PLAYER_VISIBILITY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_katg_player, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateView();
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

    @SuppressWarnings("UnusedDeclaration")
    @OnClick({R.id.seekLayout, R.id.showImageLayout})
    public void showTransport(View view) {
        Intent playbackIntent = new Intent(this.getActivity(), PlaybackControlsActivity.class);
        startActivity(playbackIntent);
    }

    @Override
    public void onStatusUpdate(final EpisodeInfoHolder episodeInfoHolder) {
        updateView();

    }

    private void updateView() {
        if (mEpisodeInfoHolder != null) {
            mEpisodeInfoTextView.setText(mEpisodeInfoHolder.getEpisodeTitle());
        }

        if (mBound) {
            switch (mMediaService.getState()) {
                case NONE:
                    break;
                case PLAYING:
                case PAUSED:
                    mIsVisible = true;
                    EpisodeInfoHolder episode = mMediaService.getEpisode();
                    mPlaybackProgressBar.setMax(episode.getEpisodeLength() * 1000);
                    mPlaybackProgressBar.setProgress(episode.getEpisodeLastPlayed());
                    mEpisodeInfoTextView.setText(episode.getEpisodeTitle());
                    break;
            }
        }

        View view = getView();
        if (view != null) {
            if (mIsVisible) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EPISODE_INFO_HOLDER, mEpisodeInfoHolder);
        outState.putBoolean(PLAYER_VISIBILITY, mIsVisible);
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

    public void loadEpisodeInfo(EpisodeInfoHolder episodeInfoHolder) {
        mEpisodeInfoHolder = episodeInfoHolder;
        updateView();
    }

    public void requestVisible(boolean isVisible) {
        mIsVisible = isVisible;
        updateView();
    }
}