package com.keithandthegirl.app.ui.main;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.ui.custom.CursorFragmentPagerAdapter;
import com.keithandthegirl.app.ui.shows.ShowFragment;
import com.keithandthegirl.app.ui.slidingtabs.SlidingTabLayout;
import com.keithandthegirl.app.utils.StringUtils;

/**
 * Created by Jeff on 11/26/2014.
 */
public class ShowsTabFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ShowsTabFragment.class.getSimpleName();

    CursorFragmentPagerAdapter mAdapter;
    private View mProgressView;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    public static ShowsTabFragment newInstance() {
        ShowsTabFragment fragment = new ShowsTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        final View rootView = inflater.inflate( R.layout.fragment_shows_tab, container, false );
        mProgressView = rootView.findViewById(R.id.progressContainer);
        mProgressView.setVisibility(View.VISIBLE);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mAdapter = new ShowCursorAdapter(getFragmentManager(), null);
        mViewPager.setAdapter(mAdapter);

        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );
        getLoaderManager().initLoader( 0, getArguments(), this );
    }


    @Override
    public Loader<Cursor> onCreateLoader( int i, Bundle args ) {
        String[] projection = { ShowConstants._ID, ShowConstants.FIELD_NAME, ShowConstants.FIELD_PREFIX, ShowConstants.FIELD_COVERIMAGEURL_200, ShowConstants.FIELD_VIP, ShowConstants.FIELD_EPISODE_COUNT_NEW };
        String selection = null;
        String[] selectionArgs = null;

        CursorLoader cursorLoader = new CursorLoader( getActivity(), ShowConstants.CONTENT_URI, projection, selection, selectionArgs, ShowConstants.FIELD_SORTORDER );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished( Loader<Cursor> cursorLoader, Cursor cursor ) {
        if (cursor.getCount() > 0) {
            mProgressView.setVisibility(View.GONE);
        }
        mAdapter.swapCursor(cursor);
        mSlidingTabLayout.updateTabStrip();
    }

    @Override
    public void onLoaderReset( Loader<Cursor> cursorLoader ) {
        mAdapter.swapCursor( null );
    }


    private class ShowCursorAdapter extends CursorFragmentPagerAdapter {

        public ShowCursorAdapter( FragmentManager fragmentManager, Cursor cursor ) {
            super(fragmentManager, cursor, true);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = StringUtils.EMPTY_STRING;
            if (mCursor.moveToPosition(position)) {
                title = mCursor.getString(mCursor.getColumnIndex(ShowConstants.FIELD_NAME));
            }
            return title;
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            String name = mCursor.getString(mCursor.getColumnIndex(ShowConstants.FIELD_NAME));
            String prefix = mCursor.getString(mCursor.getColumnIndex(ShowConstants.FIELD_PREFIX));
            String coverUrl = mCursor.getString(mCursor.getColumnIndex(ShowConstants.FIELD_COVERIMAGEURL_200));
            boolean vip = mCursor.getLong(mCursor.getColumnIndex(ShowConstants.FIELD_VIP)) == 0 ? false : true;
            long newShows = mCursor.getInt(mCursor.getColumnIndex(ShowConstants.FIELD_EPISODE_COUNT_NEW));

            int showId = mCursor.getInt(mCursor.getColumnIndex(ShowConstants._ID));
            return ShowFragment.newInstance(showId);
        }
    }
}
