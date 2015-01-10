package com.keithandthegirl.app.ui.player;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.keithandthegirl.app.R;

import butterknife.ButterKnife;

public class DetailPlayerControlsActivity extends ActionBarActivity {
    public static final String ARG_SHOW_ID = "arg_show_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int showId = getIntent().getIntExtra(ARG_SHOW_ID, -1);
        setContentView(R.layout.activity_detail_player_controls);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, PlaceholderFragment.newInstance(showId))
                    .commit();
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private int mShowId;

        public static PlaceholderFragment newInstance(int showId) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SHOW_ID, showId);
            fragment.setArguments(args);
            return fragment;
        }   
        
        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mShowId = getArguments().getInt(ARG_SHOW_ID);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail_player_controls, container, false);
            ButterKnife.inject(this, rootView);
            return rootView;
        }
    }
}
