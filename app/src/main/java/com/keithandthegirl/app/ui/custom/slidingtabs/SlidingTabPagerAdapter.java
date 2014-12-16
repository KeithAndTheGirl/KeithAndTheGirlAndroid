package com.keithandthegirl.app.ui.custom.slidingtabs;

import android.database.Cursor;
import android.support.v4.app.FragmentManager;

import com.keithandthegirl.app.ui.custom.CursorFragmentPagerAdapter;

/**
 * Created by Jeff on 12/2/2014.
 */
public abstract class SlidingTabPagerAdapter extends CursorFragmentPagerAdapter {
    private static final String TAG = SlidingTabPagerAdapter.class.getName();

    public SlidingTabPagerAdapter(FragmentManager fragmentManager, Cursor c, int flags) {
        super(fragmentManager, c, flags);
    }

    public SlidingTabPagerAdapter(FragmentManager fragmentManager, Cursor cursor, boolean autoRequery) {
        super(fragmentManager, cursor, autoRequery);
    }

    public abstract boolean isVip(int position);
    public abstract boolean hasNewShows(int position);
    public abstract int getNewShowCount(int position);
}
