package com.keithandthegirl.app.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.services.media.MediaService;
import com.keithandthegirl.app.services.media.MediaService.MediaServiceBinder;
import com.keithandthegirl.app.services.media.MediaService.State;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Jeff on 12/16/2014.
 */
public class KatgPlayerFragment extends Fragment {

    private static final String TAG = KatgPlayerFragment.class.getSimpleName();

    MediaService mService;
    boolean mBound = false;

    @InjectView(R.id.seekLayout)
    View seekLayout;
    @InjectView(R.id.showImageLayout)
    View showImageLayout;
    @InjectView(R.id.playImageButton)
    ImageButton playImageButton;
    @InjectView(R.id.playbackProgressBar)
    ProgressBar playbackProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_katg_player, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d( TAG, "onStart : connecting to MediaService" );
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
            Log.d( TAG, "onStop : disconnecting from MediaService" );
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @OnClick( {R.id.seekLayout, R.id.showImageLayout})
    public void showTransport(View view) {
        Intent playbackIntent = new Intent(this.getActivity(), DetailPlayerControlsActivity.class);
        startActivity(playbackIntent);
    }

    private void updateView() {
        Log.d( TAG, "updateView : enter" );

        if( mBound ) {
            Log.d( TAG, "updateView : MediaService bound, setting up controls" );

            if( mService.getState().equals( State.Playing ) || mService.getState().equals( State.Paused ) ) {
                Log.d( TAG, "updateView : MediaService is playing" );

                EpisodeInfoHolder episode = mService.getEpisode();
                playbackProgressBar.setMax( episode.getEpisodeLength() * 1000 );
                playbackProgressBar.setProgress( episode.getEpisodeLastPlayed() );

                // todo: toggle play button
            }

        }

        Log.d( TAG, "updateView : exit" );
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected( ComponentName className, IBinder service ) {
            Log.d( TAG, "ServiceConnection.onServiceConnected : enter" );

            MediaServiceBinder binder = (MediaServiceBinder) service;
            mService = binder.getService();
            mBound = true;

            updateView();

            Log.d( TAG, "ServiceConnection.onServiceConnected : exit" );
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d( TAG, "ServiceConnection.onServiceDisconnected : enter" );

            mBound = false;

            Log.d( TAG, "ServiceConnection.onServiceDisconnected : exit" );
        }

    };

}
