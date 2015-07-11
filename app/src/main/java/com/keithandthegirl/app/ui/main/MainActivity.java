package com.keithandthegirl.app.ui.main;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.episode.EpisodeFragment;
import com.keithandthegirl.app.ui.settings.SettingsActivity;
import com.keithandthegirl.app.ui.shows.ShowFragment;

public class MainActivity extends AbstractBaseActivity implements ShowFragment.OnShowFragmentListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mPlayerFragment =
//                (PlaybackStatusFragment) getSupportFragmentManager().findFragmentById(R.id.katgToolbarPlayer);

        boolean neverRun = false;
        Cursor cursor = getContentResolver().query(ShowConstants.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() == 0) {
            neverRun = true;
        }
        cursor.close();

        if (neverRun) {
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        }

        getSupportActionBar().setTitle( getResources().getString( R.string.menu_item_shows ) );

        replaceFragment( ShowsTabFragment.newInstance() );

    }

    public void onVipButtonClicked() {

        Intent settingsIntent = new Intent( this, SettingsActivity.class );
        startActivity( settingsIntent );

    }

    @Override
    public void onShowSelected( long showId, long episodeId ) {

        replaceFragment( EpisodeFragment.newInstance( episodeId ) );

    }

}
