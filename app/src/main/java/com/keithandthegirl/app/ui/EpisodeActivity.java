package com.keithandthegirl.app.ui;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.Toast;

import com.keithandthegirl.app.MainApplication;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.db.schedule.KatgAlarmReceiver;
import com.keithandthegirl.app.ui.shows.EpisodeFragment;

public class EpisodeActivity extends AbstractBaseActivity {

    private static final String TAG = EpisodeActivity.class.getSimpleName();

    public static final String EPISODE_KEY = "episode_id";

    long mEpisodeId;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        Log.d( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_episode );

        Bundle extras = getIntent().getExtras();
        if( extras.containsKey( EPISODE_KEY ) ) {
            mEpisodeId = extras.getLong( EPISODE_KEY );

            updateView();
        } else {
            Toast.makeText( this, "Episode not found!", Toast.LENGTH_LONG ).show();

            finish();
        }

        Log.d( TAG, "onCreate : exit" );
    }

    private void updateView() {
        Log.v( TAG, "updateView : enter" );

        ActionBar actionBar = getSupportActionBar();

        int showNameId = -1;
        Cursor cursor = getContentResolver().query( ContentUris.withAppendedId( Episode.CONTENT_URI, mEpisodeId ), null, null, null, null );
        if( cursor.moveToNext() ) {

            showNameId = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_SHOWNAMEID ) );

        }
        cursor.close();

        if( showNameId > 0 ) {

            cursor = getContentResolver().query( ContentUris.withAppendedId( Show.CONTENT_URI, showNameId ), null, null, null, null );
            if( cursor.moveToNext() ) {

                String showName = cursor.getString( cursor.getColumnIndex( Show.FIELD_NAME ) );
                actionBar.setTitle( showName );
            }
            cursor.close();

        }

        EpisodeFragment episodeFragment = new EpisodeFragment();
        episodeFragment.setArguments( getIntent().getExtras() );
        getSupportFragmentManager().beginTransaction().add( R.id.container, episodeFragment ).commit();

        Log.v( TAG, "updateView : exit" );
    }

}
