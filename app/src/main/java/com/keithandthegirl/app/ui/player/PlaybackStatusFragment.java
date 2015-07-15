package com.keithandthegirl.app.ui.player;

import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.services.media.MediaService;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Jeff on 12/16/2014.
 */
public class PlaybackStatusFragment extends Fragment {

    private static final String TAG = PlaybackStatusFragment.class.getSimpleName();

    private static final String EPISODE_INFO_HOLDER = "EPISODE_INFO_HOLDER";
    private static final String PLAYER_VISIBILITY = "PLAYER_VISIBILITY";

    MediaService mMediaService;
    boolean mBound = false;

    @Bind( R.id.seekLayout )
    View mSeekLayout;

    @Bind( R.id.playImageButton )
    ImageButton mPlayImageButton;

    @Bind( R.id.playbackProgressBar )
    ProgressBar mPlaybackProgressBar;

    @Bind( R.id.episodeInfoTextView )
    TextView mEpisodeInfoTextView;

    @Bind( R.id.extra_info )
    TextView mExtraInfo;

    private EpisodeInfoHolder mEpisodeInfoHolder;
    private boolean mIsVisible;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        if( savedInstanceState != null ) {

            mEpisodeInfoHolder = savedInstanceState.getParcelable( EPISODE_INFO_HOLDER );
            mIsVisible = savedInstanceState.getBoolean( PLAYER_VISIBILITY );

        }

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View rootView = inflater.inflate( R.layout.fragment_katg_player, container, false );
        ButterKnife.bind( this, rootView );

        mPlayImageButton.setEnabled( true );
        mPlayImageButton.setOnClickListener( mPlayImageButtonListener );

        rootView.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {

                Intent playbackIntent = new Intent( getActivity(), PlaybackControlsActivity.class );
                playbackIntent.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );
                startActivity( playbackIntent );

            }

        });

        return rootView;
    }

    @Override
    public void onViewCreated( final View view, @Nullable final Bundle savedInstanceState ) {
        super.onViewCreated( view, savedInstanceState );

        updateView();

    }

    @Override
    public void onStart() {
        super.onStart();

//        if (getActivity().getMediaController() != null) {
//            onConnected();
//        }

    }

    @Override
    public void onResume() {
        super.onResume();

        updateView();
    }

    @Override
    public void onStop() {
        super.onStop();

//        if (getActivity().getMediaController() != null) {
//            getActivity().getMediaController().unregisterCallback(mCallback);
//        }

    }

//    @SuppressWarnings( "UnusedDeclaration" )
//    @OnClick( { R.id.seekLayout, R.id.showImageLayout } )
//    public void showTransport( View view ) {
//
//        Intent playbackIntent = new Intent( this.getActivity(), PlaybackControlsActivity.class );
//        startActivity(playbackIntent);
//
//    }

    private void updateView() {

        if( mEpisodeInfoHolder != null ) {

            mEpisodeInfoTextView.setText( mEpisodeInfoHolder.getEpisodeTitle() );

        }

        if( mBound ) {

            switch( mMediaService.getState() ) {

                case NONE:

                    break;

                case PLAYING:
                case PAUSED:

                    mIsVisible = true;
                    EpisodeInfoHolder episode = mMediaService.getEpisode();
                    mPlaybackProgressBar.setMax( episode.getEpisodeLength() * 1000 );
                    mPlaybackProgressBar.setProgress( episode.getEpisodeLastPlayed() );
                    mEpisodeInfoTextView.setText( episode.getEpisodeTitle() );

                    break;

            }

        }

//        View view = getView();
//        if( view != null ) {
//            if( mIsVisible ) {
//
//                view.setVisibility( View.VISIBLE );
//
//            } else {
//
//                view.setVisibility( View.GONE );
//
//            }
//
//        }

    }

//    @Override
//    public void onSaveInstanceState( final Bundle outState ) {
//        super.onSaveInstanceState( outState );
//
//        outState.putParcelable( EPISODE_INFO_HOLDER, mEpisodeInfoHolder );
//        outState.putBoolean( PLAYER_VISIBILITY, mIsVisible );
//
//    }

//    private ServiceConnection mConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected( ComponentName className, IBinder service ) {
//
//            MediaServiceBinder binder = (MediaServiceBinder) service;
//            mMediaService = binder.getMediaService();
//            mMediaService.setMediaServiceEventListener( PlaybackStatusFragment.this );
//            mBound = true;
//
//            updateView();
//
//        }
//
//        @Override
//        public void onServiceDisconnected( ComponentName arg0 ) {
//            mBound = false;
//        }
//
//    };

    public void loadEpisodeInfo( EpisodeInfoHolder episodeInfoHolder) {

        mEpisodeInfoHolder = episodeInfoHolder;
        updateView();

    }

    public void onConnected() {
//        MediaController controller = getActivity().getMediaController();
//        LogHelper.d(TAG, "onConnected, mediaController==null? ", controller == null);
//        if (controller != null) {
//            onMetadataChanged(controller.getMetadata());
//            onPlaybackStateChanged(controller.getPlaybackState());
//            controller.registerCallback(mCallback);
//        }
    }

    private void onMetadataChanged(MediaMetadata metadata) {
//        LogHelper.d(TAG, "onMetadataChanged ", metadata);
//        if (getActivity() == null) {
//            LogHelper.w(TAG, "onMetadataChanged called when getActivity null," +
//                    "this should not happen if the callback was properly unregistered. Ignoring.");
//            return;
//        }
//        if (metadata == null) {
//            return;
//        }
//
//        mTitle.setText(metadata.getDescription().getTitle());
//        mSubtitle.setText(metadata.getDescription().getSubtitle());
//        String artUrl = null;
//        if (metadata.getDescription().getIconUri() != null) {
//            artUrl = metadata.getDescription().getIconUri().toString();
//        }
//        if (!TextUtils.equals(artUrl, mArtUrl)) {
//            mArtUrl = artUrl;
//            Bitmap art = metadata.getDescription().getIconBitmap();
//            AlbumArtCache cache = AlbumArtCache.getInstance();
//            if (art == null) {
//                art = cache.getIconImage(mArtUrl);
//            }
//            if (art != null) {
//                mAlbumArt.setImageBitmap(art);
//            } else {
//                cache.fetch(artUrl, new AlbumArtCache.FetchListener() {
//                            @Override
//                            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
//                                if (icon != null) {
//                                    LogHelper.d(TAG, "album art icon of w=", icon.getWidth(),
//                                            " h=", icon.getHeight());
//                                    if (isAdded()) {
//                                        mAlbumArt.setImageBitmap(icon);
//                                    }
//                                }
//                            }
//                        }
//                );
//            }
//        }
    }

    public void setExtraInfo( String extraInfo ) {
        if( extraInfo == null ) {

            mExtraInfo.setVisibility( View.GONE );

        } else {

            mExtraInfo.setText( extraInfo );
            mExtraInfo.setVisibility( View.VISIBLE );

        }

    }

    private void onPlaybackStateChanged( PlaybackState state ) {
//        Log.d( TAG, "onPlaybackStateChanged ", state );

        if( getActivity() == null ) {
            Log.w( TAG, "onPlaybackStateChanged called when getActivity null, this should not happen if the callback was properly unregistered. Ignoring." );

            return;
        }

        if (state == null) {

            return;
        }

        boolean enablePlay = false;
        switch (state.getState()) {
            case PlaybackState.STATE_PAUSED:
            case PlaybackState.STATE_STOPPED:
                enablePlay = true;
                break;
            case PlaybackState.STATE_ERROR:
//                Log.e(TAG, "error playbackstate: ", state.getErrorMessage());
                Toast.makeText(getActivity(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
        }

        if( enablePlay ) {

            mPlayImageButton.setImageDrawable( getActivity().getDrawable( R.drawable.ic_play_arrow_grey600_48dp ) );

        } else {

            mPlayImageButton.setImageDrawable( getActivity().getDrawable( R.drawable.ic_pause_grey600_48dp ) );

        }

//        MediaController controller = getActivity().getMediaController();
        String extraInfo = null;
//        if( controller != null && controller.getExtras() != null ) {
//
//            String castName = controller.getExtras().getString( MusicService.EXTRA_CONNECTED_CAST );
//            if( castName != null ) {
//
//                extraInfo = getResources().getString( R.string.casting_to_device, castName );
//
//            }
//
//        }

        setExtraInfo( extraInfo );

    }

    private final View.OnClickListener mPlayImageButtonListener = new View.OnClickListener() {

        @Override
        public void onClick( View v ) {

//            PlaybackState stateObj = getActivity().getMediaController().getPlaybackState();
//            final int state = stateObj == null ? PlaybackState.STATE_NONE : stateObj.getState();
//            Log.d( TAG, "Button pressed, in state " + state );
//
//            switch( v.getId() ) {
//
//                case R.id.playImageButton:
//                    Log.d( TAG, "Play button pressed, in state " + state );
//                    if( state == PlaybackState.STATE_PAUSED ||
//                            state == PlaybackState.STATE_STOPPED ||
//                            state == PlaybackState.STATE_NONE) {
//                        playMedia();
//                    } else if (state == PlaybackState.STATE_PLAYING ||
//                            state == PlaybackState.STATE_BUFFERING ||
//                            state == PlaybackState.STATE_CONNECTING) {
//                        pauseMedia();
//                    }
//                    break;
//
//            }

        }

    };

    private void playMedia() {

//        MediaController controller = getActivity().getMediaController();
//        if( controller != null ) {
//
//            controller.getTransportControls().play();
//
//        }

    }

    private void pauseMedia() {

//        MediaController controller = getActivity().getMediaController();
//        if( controller != null ) {
//
//            controller.getTransportControls().pause();
//
//        }

    }

}