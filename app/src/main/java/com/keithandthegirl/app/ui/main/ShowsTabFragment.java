package com.keithandthegirl.app.ui.main;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keithandthegirl.app.BuildConfig;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.sync.ShowsDataFragment;
import com.keithandthegirl.app.ui.custom.CursorFragmentPagerAdapter;
import com.keithandthegirl.app.ui.shows.ShowFragment;
import com.keithandthegirl.app.ui.custom.slidingtabs.SlidingTabLayout;
import com.keithandthegirl.app.ui.custom.slidingtabs.SlidingTabPagerAdapter;
import com.keithandthegirl.app.utils.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Jeff on 11/26/2014.
 * TODO remember page when coming back from replaceFragment
 */
public class ShowsTabFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ShowsTabFragment.class.getSimpleName();
    private static final String SHOWS_DATA_FRAGMENT_TAG = ShowsDataFragment.class.getCanonicalName();

    CursorFragmentPagerAdapter mAdapter;
    @InjectView(R.id.progressContainer)
    View mProgressView;
    @InjectView(R.id.viewpager)
    ViewPager mViewPager;
    @InjectView(R.id.sliding_tabs)
    SlidingTabLayout mSlidingTabLayout;

    public static ShowsTabFragment newInstance() {
        ShowsTabFragment fragment = new ShowsTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_shows_tab, container, false);
        ButterKnife.inject(this, rootView);

        ShowsDataFragment showsDataFragment = (ShowsDataFragment) getChildFragmentManager().findFragmentByTag(SHOWS_DATA_FRAGMENT_TAG);
        if (null == showsDataFragment) {
            showsDataFragment = (ShowsDataFragment) instantiate(getActivity(), ShowsDataFragment.class.getName());
            showsDataFragment.setRetainInstance(true);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.add(showsDataFragment, SHOWS_DATA_FRAGMENT_TAG);
            transaction.commit();
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressView.setVisibility(View.VISIBLE);
        mAdapter = new ShowsCursorAdapter(getChildFragmentManager(), null);
        mViewPager.setAdapter(mAdapter);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        String[] projection = {
                ShowConstants._ID,
                ShowConstants.FIELD_NAME,
                ShowConstants.FIELD_PREFIX,
                ShowConstants.FIELD_COVERIMAGEURL_200,
                ShowConstants.FIELD_VIP,
                ShowConstants.FIELD_EPISODE_COUNT_NEW};

        String selection = null;
        String[] selectionArgs = null;

        CursorLoader cursorLoader = new CursorLoader(getActivity(), ShowConstants.CONTENT_URI, projection, selection, selectionArgs, ShowConstants.FIELD_SORTORDER);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            mProgressView.setVisibility(View.GONE);
        }
        mAdapter.swapCursor(cursor);
        mSlidingTabLayout.updateTabStrip();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }


    private class ShowsCursorAdapter extends SlidingTabPagerAdapter {

        public ShowsCursorAdapter(FragmentManager fragmentManager, Cursor cursor) {
            super(fragmentManager, cursor, true);
        }

        @Override
        public boolean isVip(int position) {
            boolean isVip = false;
            if (mCursor.moveToPosition(position)) {
                isVip = mCursor.getLong(mCursor.getColumnIndex(ShowConstants.FIELD_VIP)) == 0 ? false : true;
            }
            return isVip;
        }

        @Override
        public boolean hasNewShows(int position) {
            boolean hasNewShows = false;
            if (position == 1) {
                if (mCursor.moveToPosition(position)) {
                    long newShows = mCursor.getInt(mCursor.getColumnIndex(ShowConstants.FIELD_EPISODE_COUNT_NEW));
                    if (newShows > 0) {
                        hasNewShows = true;
                    }
                }
            }
            return hasNewShows;
        }

        @Override
        public int getNewShowCount(int position) {
            int newShowCount = 0;
            if (mCursor.moveToPosition(position)) {
                newShowCount = mCursor.getInt(mCursor.getColumnIndex(ShowConstants.FIELD_EPISODE_COUNT_NEW));
            }
            return newShowCount;
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

            if (BuildConfig.DEBUG) {
                String name = mCursor.getString(mCursor.getColumnIndex(ShowConstants.FIELD_NAME));
                String prefix = mCursor.getString(mCursor.getColumnIndex(ShowConstants.FIELD_PREFIX));
                String coverUrl = mCursor.getString(mCursor.getColumnIndex(ShowConstants.FIELD_COVERIMAGEURL_200));
                boolean vip = mCursor.getLong(mCursor.getColumnIndex(ShowConstants.FIELD_VIP)) == 0 ? false : true;
                long newShows = mCursor.getInt(mCursor.getColumnIndex(ShowConstants.FIELD_EPISODE_COUNT_NEW));
            }
            int showId = mCursor.getInt(mCursor.getColumnIndex(ShowConstants._ID));
            return ShowFragment.newInstance(showId);
        }
    }
}
