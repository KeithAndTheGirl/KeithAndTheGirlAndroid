package com.keithandthegirl.app.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.ui.utils.WorkFragment;

/**
 * Created by dmfrey on 5/6/14.
 */
public class WorkItemsActivity extends AbstractBaseActivity {

    private static final String TAG = WorkItemsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_workitems );

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled( false );

        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled( true );

        FragmentManager fm = getSupportFragmentManager();

        WorkFragment workFragment = new WorkFragment();
        fm.beginTransaction().replace( R.id.container, workFragment ).commit();
    }
}
