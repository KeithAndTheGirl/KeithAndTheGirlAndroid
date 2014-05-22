package com.keithandthegirl.app.ui;

import android.content.Intent;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.ui.youtube.DeveloperKey;

/**
 * Created by dmfrey on 5/22/14.
 */
public abstract class YouTubeFailureRecoveryActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final String TAG = YouTubeFailureRecoveryActivity.class.getSimpleName();

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    @Override
    public void onInitializationFailure( YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason ) {

        if( errorReason.isUserRecoverableError() ) {

            errorReason.getErrorDialog( this, RECOVERY_DIALOG_REQUEST ).show();

        } else {

            String errorMessage = String.format(getString( R.string.error_player ), errorReason.toString() );
            Toast.makeText( this, errorMessage, Toast.LENGTH_LONG ).show();

        }

    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {

        if( requestCode == RECOVERY_DIALOG_REQUEST ) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize( DeveloperKey.DEVELOPER_KEY, this );
        }

    }

    protected abstract YouTubePlayer.Provider getYouTubePlayerProvider();

}
