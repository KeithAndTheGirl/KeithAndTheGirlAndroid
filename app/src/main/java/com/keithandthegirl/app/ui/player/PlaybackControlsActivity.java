package com.keithandthegirl.app.ui.player;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.keithandthegirl.app.R;

public class PlaybackControlsActivity extends ActionBarActivity {
    private static final String TAG = PlaybackControlsActivity.class.getSimpleName();

    public static final String ARG_SHOW_ID = "arg_show_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int showId = getIntent().getIntExtra(ARG_SHOW_ID, -1);
        setContentView(R.layout.activity_detail_player_controls);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, PlaybackControlsFragment.newInstance(showId))
                    .commit();
        }
    }
}
