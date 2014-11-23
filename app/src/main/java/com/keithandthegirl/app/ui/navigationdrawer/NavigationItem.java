package com.keithandthegirl.app.ui.navigationdrawer;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.keithandthegirl.app.utils.StringUtils;

public final class NavigationItem {
    private static final String TAG = NavigationItem.class.getSimpleName();
    private Class<? extends Fragment> mClass;
    private Fragment mFragment;
    private boolean mAddToBackStack;
    private String mBackStackName = null;
    private Bundle mBundle;
    private String mTitle;

    private int mLabelId;
    private int mResIcon;
    private boolean mVip;

    public NavigationItem(Class<? extends Fragment> aClass) {
        mClass = aClass;
        
        mAddToBackStack = false;
        mBundle = null;
    }

    public Fragment getFragment() {
        if (mFragment == null) {
            try {
                mFragment = mClass.newInstance();
            } catch (Exception e) {
                Log.e(TAG, "Failed instantiating fragment", e);
            }
        }
        return mFragment;
    }

    public boolean isAddToBackStack() {
        return mAddToBackStack;
    }

    public void setAddToBackStack(boolean addToBackStack) {
        mAddToBackStack = addToBackStack;
    }

    public String getBackStackName() {
        if (StringUtils.isNullOrEmpty(mBackStackName)) {
            return mClass.getSimpleName();
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
    
    public void setTargetFragment(Fragment fragment, int requestCode) {
    	getFragment().setTargetFragment(fragment, requestCode);
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

    public void setIcon(@DrawableRes int mResIcon) {
        this.mResIcon = mResIcon;
    }

    public int getLabelId() {
        return mLabelId;
    }

    public void setLabelId(@StringRes int labelId) {
        this.mLabelId = labelId;
    }

    public boolean isVip() {
        return mVip;
    }

    public boolean ismVip() {
        return mVip;
    }

    public void setmVip(boolean mVip) {
        this.mVip = mVip;
    }
}