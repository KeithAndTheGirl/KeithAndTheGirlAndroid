package com.keithandthegirl.app.ui.navigationdrawer;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.keithandthegirl.app.utils.StringUtils;

public final class NavigationItem {
    private static final String TAG = NavigationItem.class.getSimpleName();

    private boolean mAddToBackStack;
    private NavigationItemType mNavigationItemType;
    private String mBackStackName = null;
    private Bundle mBundle;
    private String mTitle;

    private int mLabelId;
    private int mResIcon;
    private boolean mVip;

    public enum NavigationItemType { SHOWS, GUESTS, LIVE, SCHEDULE, YOUTUBE, ABOUT, SETTINGS, UNKNOWN }

    public NavigationItem(NavigationItemType navigationItemType) {
        mNavigationItemType = navigationItemType;
        mAddToBackStack = false;
        mBundle = null;
    }

    public boolean isAddToBackStack() {
        return mAddToBackStack;
    }

    public void setAddToBackStack(boolean addToBackStack) {
        mAddToBackStack = addToBackStack;
    }

    public String getBackStackName() {
        if (StringUtils.isNullOrEmpty(mBackStackName)) {
            return mNavigationItemType.toString();
        }
        else {
            return mBackStackName;
        }
    }

    public void setBackStackName(String backStackName) {
        mBackStackName = backStackName;
    }

    public Bundle getBundle() {
        return mBundle;
    }

    public void setBundle(Bundle bundle) {
        mBundle = bundle;
    }
    
    public void setLabel(String title) {
        mTitle = title;
    }

    public String getLabel() {
        return mTitle;
    }

    public int getIcon() {
        return mResIcon;
    }

    public void setIcon(@DrawableRes int resIcon) {
        mResIcon = resIcon;
    }

    public int getLabelId() {
        return mLabelId;
    }

    public void setLabelId(@StringRes int labelId) {
        mLabelId = labelId;
    }

    public boolean isVip() {
        return mVip;
    }

    public void setVip(boolean isVip) {
        mVip = isVip;
    }

    public NavigationItemType getNavigationItemType() {
        return mNavigationItemType;
    }

    public void setNavigationItemType(NavigationItemType navigationItemType) {
        this.mNavigationItemType = navigationItemType;
    }
}