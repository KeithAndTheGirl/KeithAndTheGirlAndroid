package com.keithandthegirl.app.ui;

import android.os.Bundle;
import android.util.Log;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.ui.youtube.DeveloperKey;

/**
 * Created by dmfrey on 5/22/14.
 */
public class YoutubeFragmentActivity extends YouTubeFailureRecoveryActivity {

    private static final String TAG = YoutubeFragmentActivity.class.getSimpleName();

    public static final String YOUTUBE_VIDEO_KEY = "youtube_video_key";

    private String youtubeId;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        Log.v( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        setContentView( R.layout.fragment_youtube_player );

        if( null != savedInstanceState && savedInstanceState.containsKey( YOUTUBE_VIDEO_KEY ) ) {
            youtubeId = savedInstanceState.getString( YOUTUBE_VIDEO_KEY );
        }

        if( getIntent().hasExtra( YOUTUBE_VIDEO_KEY ) ) {
            youtubeId = getIntent().getStringExtra( YOUTUBE_VIDEO_KEY );
        }

        YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById( R.id.youtube_fragment );
        youTubePlayerFragment.initialize( DeveloperKey.DEVELOPER_KEY, this );

        Log.v( TAG, "onCreate : enter" );
    }

    @Override
    public void onRestoreInstanceState( Bundle savedInstanceState ) {
        Log.v( TAG, "onRestoreInstanceState : enter" );

        if( savedInstanceState.containsKey( YOUTUBE_VIDEO_KEY ) ) {
            Log.v( TAG, "onRestoreInstanceState : savedInstanceState contains selected navigation item" );

            youtubeId = savedInstanceState.getString( YOUTUBE_VIDEO_KEY );

        }

        Log.v( TAG, "onRestoreInstanceState : exit" );
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        Log.v( TAG, "onSaveInstanceState : enter" );

        outState.putString( YOUTUBE_VIDEO_KEY, youtubeId );

        Log.v( TAG, "onSaveInstanceState : exit" );
    }

    @Override
    public void onInitializationSuccess( YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored ) {
        Log.v( TAG, "onInitializationSuccess : enter" );

        if( !wasRestored ) {
            Log.v( TAG, "onInitializationSuccess : !wasRestored" );

            if( null != youtubeId && !"".equals( youtubeId ) ) {
                Log.v( TAG, "onInitializationSuccess : youtubeId=" + youtubeId );

                player.cueVideo( youtubeId );

            }

        }

        Log.v( TAG, "onInitializationSuccess : exit" );
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        Log.v( TAG, "getYouTubePlayerProvider : enter" );

        YouTubePlayer.Provider provider = (YouTubePlayerFragment) getFragmentManager().findFragmentById( R.id.youtube_fragment );

        Log.v( TAG, "getYouTubePlayerProvider : exit" );
        return provider;
    }

}
