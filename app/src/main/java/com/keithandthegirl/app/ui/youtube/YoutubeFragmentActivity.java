package com.keithandthegirl.app.ui.youtube;

import android.os.Bundle;
import android.util.Log;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.keithandthegirl.app.R;

/**
 * Created by dmfrey on 5/22/14.
 */
public class YoutubeFragmentActivity extends YouTubeFailureRecoveryActivity {

    private static final String TAG = YoutubeFragmentActivity.class.getSimpleName();

    public static final String YOUTUBE_VIDEO_KEY = "youtube_video_key";

    private String youtubeId;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
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
    }

    @Override
    public void onRestoreInstanceState( Bundle savedInstanceState ) {
        if( savedInstanceState.containsKey( YOUTUBE_VIDEO_KEY ) ) {
            Log.v( TAG, "onRestoreInstanceState : savedInstanceState contains selected navigation item" );

            youtubeId = savedInstanceState.getString( YOUTUBE_VIDEO_KEY );

        }
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        outState.putString( YOUTUBE_VIDEO_KEY, youtubeId );
    }

    @Override
    public void onInitializationSuccess( YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored ) {
        if( !wasRestored ) {
            Log.v( TAG, "onInitializationSuccess : !wasRestored" );

            if( null != youtubeId && !"".equals( youtubeId ) ) {
                Log.v( TAG, "onInitializationSuccess : youtubeId=" + youtubeId );

                player.cueVideo( youtubeId );

            }

        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        YouTubePlayer.Provider provider = (YouTubePlayerFragment) getFragmentManager().findFragmentById( R.id.youtube_fragment );
        return provider;
    }
}