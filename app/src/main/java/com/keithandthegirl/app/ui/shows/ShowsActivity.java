package com.keithandthegirl.app.ui.shows;

import android.app.ActionBar;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.ui.AbstractBaseActivity;

public class ShowsActivity extends AbstractBaseActivity implements ActionBar.OnNavigationListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ShowsActivity.class.getSimpleName();

    public static final String SHOW_NAME_POSITION_KEY = "showNamePositionId";

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    ShowCursorAdapter mAdapter;
    int mSelectedNavigationItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shows);

        if (null != savedInstanceState && savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            mSelectedNavigationItem = savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM);
        }

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (null != extras && extras.containsKey(SHOW_NAME_POSITION_KEY)) {
            mSelectedNavigationItem = extras.getInt(SHOW_NAME_POSITION_KEY);
        }
        Log.v(TAG, "onCreate : mSelectedNavigationItem=" + mSelectedNavigationItem);

        mAdapter = new ShowCursorAdapter(actionBar.getThemedContext());

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(mAdapter, this);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            Log.v(TAG, "onRestoreInstanceState : savedInstanceState contains selected navigation item");

            getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        mSelectedNavigationItem = position;

        // When the given dropdown item is selected, show its contents in the
        // container view.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ShowFragment.newInstance(id))
                .commit();
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        String[] projection = {ShowConstants._ID, ShowConstants.FIELD_NAME};
        String selection = null;
        String[] selectionArgs = null;

        CursorLoader cursorLoader = new CursorLoader(this, ShowConstants.CONTENT_URI, projection, selection, selectionArgs, ShowConstants.FIELD_SORTORDER);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);

        final ActionBar actionBar = getActionBar();
        actionBar.setSelectedNavigationItem(mSelectedNavigationItem);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    private class ShowCursorAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        public ShowCursorAdapter(Context context) {
            super(context, null, false);

            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            View view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);

            ViewHolder refHolder = new ViewHolder();
            refHolder.name = (TextView) view.findViewById(android.R.id.text1);

            view.setTag(refHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            ViewHolder mHolder = (ViewHolder) view.getTag();

            String name = cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_NAME));
            mHolder.name.setText(name);
        }
    }

    private static class ViewHolder {
        TextView name;

        ViewHolder() {
        }
    }
}
