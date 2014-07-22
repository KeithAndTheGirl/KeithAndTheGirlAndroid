package com.keithandthegirl.app.ui;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.Episode;

public class VideoPlayerActivity extends ActionBarActivity {

    private static final String TAG = VideoPlayerActivity.class.getSimpleName();

    public static final String EPISODE_KEY = "episode_key";
    public static final String VIDEO_TYPE_KEY = "video_type_key";

    private VideoView video;
    private MediaController ctlr;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        Log.d( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        getWindow().setFormat( PixelFormat.TRANSLUCENT );

        setContentView( R.layout.activity_video_player );

        String videoType = getIntent().getStringExtra( VIDEO_TYPE_KEY );

        long episodeId = getIntent().getLongExtra( EPISODE_KEY, -1 );
        if( episodeId != -1 ) {

            String videoUrl = "";
            Cursor cursor = getContentResolver().query( ContentUris.withAppendedId( Episode.CONTENT_URI, episodeId ), null, null, null, null );
            if( cursor.moveToNext() ) {

                if( videoType.equals( "PREVIEW" ) ) {
                    videoUrl = cursor.getString( cursor.getColumnIndex( Episode.FIELD_PREVIEWURL ) );
                }

                if( videoType.equals( "HLS" ) ) {
                    videoUrl = cursor.getString(( cursor.getColumnIndex( Episode.FIELD_VIDEOFILEURL ) ) );
                }

            }
            cursor.close();

            if( !"".equals( videoUrl ) ) {
                Log.i( TAG, "onCreate : videoUrl=" + videoUrl );

                video = (VideoView) findViewById( R.id.video );
                video.setVideoURI( Uri.parse( videoUrl ) );

                ctlr = new MediaController( this );
                ctlr.setMediaPlayer( video );
                video.setMediaController( ctlr );
                video.requestFocus();
                video.start();

            }

        }

        Log.d( TAG, "onCreate : exit" );
    }

}
