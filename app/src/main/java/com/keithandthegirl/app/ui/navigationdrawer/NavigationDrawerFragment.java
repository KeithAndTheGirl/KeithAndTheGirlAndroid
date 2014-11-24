package com.keithandthegirl.app.ui.navigationdrawer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.keithandthegirl.app.R;

import java.util.List;

public class NavigationDrawerFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = NavigationDrawerFragment.class.getSimpleName();

    public enum NavigationChoice {
        ABOUT,
        LIVE,
        SCHEDULE,
        YOUTUBE,
        FEEDBACK,
        KATG,
        KATG_TV,
        WHATS_MY_NAME,
        MY_NAME_IS_KEITH,
        THATS_THE_SHOW_WITH_DANNY,
        BROTHER_LOVE_OWWWR,
        BOTTOMS_UP,
        SUPER_HANG,
        MYKA_FOX_AND_FRIENDS,
        INTERNMENT,
        KATG_BEGINNINGS,
        KATG_LIVE_SHOWS
    }

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String STATE_IS_VIP_OPEN = "state_is_vip_open";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationDrawerCallbacks mNavigationDrawerCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private boolean mIsVipExpanded = false;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private View mVipShowsLayout;
    private View mVipShowsNavView;
    private Button mVipButton;
    private View mAboutNavView;
    private View mKatgNavView;
    private View mKatgTvNavView;
    private View mWmnNaveView;
    private View mMnikNavView;
    private View mTtswdNavView;
    private View mTbloNavView;
    private View mBuwhNavView;
    private View mSuperHangNavView;
    private View mMfafNavView;
    private View mInternmentNavView;
    private View mKatgBeginningsNavView;
    private View mKatgLiveShowsNavView;
    private View mScheduleNavView;
    private View mYoutubeNavView;
    private View mSettingsNavView;
    private View mFeedbackNavView;

    public NavigationDrawerFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
            mIsVipExpanded = savedInstanceState.getBoolean(STATE_IS_VIP_OPEN, false);
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate( R.layout.fragment_navigation_drawer, container, false);

        mAboutNavView = fragmentView.findViewById(R.id.aboutNavView);
        mAboutNavView.setOnClickListener(this);

        mKatgNavView = fragmentView.findViewById(R.id.katgNavView);
        mKatgNavView.setOnClickListener(this);

        mVipShowsNavView = fragmentView.findViewById(R.id.vipShowsNavView);
        mVipShowsNavView.setOnClickListener(this);

        mVipButton = (Button)fragmentView.findViewById(R.id.vipButton);
        mVipButton.setOnClickListener(this);

        mVipShowsLayout = fragmentView.findViewById(R.id.vipShowsLayout);
        mVipShowsLayout.setOnClickListener(this);

        mKatgTvNavView = fragmentView.findViewById(R.id.katgTvNavView);
        mKatgTvNavView.setOnClickListener(this);

        mWmnNaveView = fragmentView.findViewById(R.id.wmnNavView);
        mWmnNaveView.setOnClickListener(this);

        mMnikNavView = fragmentView.findViewById(R.id.mnikNavView);
        mMnikNavView.setOnClickListener(this);

        mTtswdNavView = fragmentView.findViewById(R.id.ttswdNavView);
        mTtswdNavView.setOnClickListener(this);

        mTbloNavView = fragmentView.findViewById(R.id.tbloNavView);
        mTbloNavView.setOnClickListener(this);

        mBuwhNavView = fragmentView.findViewById(R.id.buwhNavView);
        mBuwhNavView.setOnClickListener(this);

        mSuperHangNavView = fragmentView.findViewById(R.id.superHangNavView);
        mSuperHangNavView.setOnClickListener(this);

        mMfafNavView = fragmentView.findViewById(R.id.mfafNavView);
        mMfafNavView.setOnClickListener(this);

        mInternmentNavView = fragmentView.findViewById(R.id.internmentNavView);
        mInternmentNavView.setOnClickListener(this);

        mKatgBeginningsNavView = fragmentView.findViewById(R.id.katgBeginningsNavView);
        mKatgBeginningsNavView.setOnClickListener(this);

        mKatgLiveShowsNavView = fragmentView.findViewById(R.id.katgLiveShowsNavView);
        mKatgLiveShowsNavView.setOnClickListener(this);

        mScheduleNavView = fragmentView.findViewById(R.id.scheduleNavView);
        mScheduleNavView.setOnClickListener(this);

        mYoutubeNavView = fragmentView.findViewById(R.id.youtubeNavView);
        mYoutubeNavView.setOnClickListener(this);

        mFeedbackNavView = fragmentView.findViewById(R.id.feedbackNavView);
        mFeedbackNavView.setOnClickListener(this);

        return fragmentView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aboutNavView:
                notifyCallback(NavigationChoice.ABOUT);
                break;
            case R.id.katgNavView:
                notifyCallback(NavigationChoice.KATG);
                break;
            case R.id.vipShowsNavView:
                if (mIsVipExpanded) {
                    mVipShowsLayout.setVisibility(View.GONE);
                    mIsVipExpanded = false;
                } else {
                    mVipShowsLayout.setVisibility(View.VISIBLE);
                    mIsVipExpanded = true;
                }
                break;
            case R.id.katgTvNavView:
                notifyCallback(NavigationChoice.KATG_TV);
                break;
            case R.id.wmnNavView:
                notifyCallback(NavigationChoice.WHATS_MY_NAME);
                break;
            case R.id.mnikNavView:
                notifyCallback(NavigationChoice.MY_NAME_IS_KEITH);
                break;
            case R.id.ttswdNavView:
                notifyCallback(NavigationChoice.THATS_THE_SHOW_WITH_DANNY);
                break;
            case R.id.tbloNavView:
                notifyCallback(NavigationChoice.BROTHER_LOVE_OWWWR);
                break;
            case R.id.buwhNavView:
                notifyCallback(NavigationChoice.BOTTOMS_UP);
                break;
            case R.id.superHangNavView:
                notifyCallback(NavigationChoice.SUPER_HANG);
                break;
            case R.id.mfafNavView:
                notifyCallback(NavigationChoice.MYKA_FOX_AND_FRIENDS);
                break;
            case R.id.internmentNavView:
                notifyCallback(NavigationChoice.INTERNMENT);
                break;
            case R.id.katgBeginningsNavView:
                notifyCallback(NavigationChoice.KATG_BEGINNINGS);
                break;
            case R.id.katgLiveShowsNavView:
                notifyCallback(NavigationChoice.KATG_LIVE_SHOWS);
                break;
            case R.id.scheduleNavView:
                notifyCallback(NavigationChoice.SCHEDULE);
                break;
            case R.id.youtubeNavView:
                notifyCallback(NavigationChoice.YOUTUBE);
                break;
            case R.id.feedbackNavView:
                notifyCallback(NavigationChoice.FEEDBACK);
                break;
        }
    }

    private void notifyCallback(NavigationChoice navigationChoice) {
        mNavigationDrawerCallbacks.onNavigationDrawerItemSelected(navigationChoice);
        mDrawerLayout.closeDrawer(GravityCompat.START);

    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setupDrawer(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle =  new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void setupDrawerItems(List<NavigationItem> navigationItemList) {
    }


    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mNavigationDrawerCallbacks != null) {
//            if (mNavigationItemsList.size() > 0 ){
//                mNavigationDrawerCallbacks.onNavigationDrawerItemSelected(mNavigationItemsList.get(position));
//            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof NavigationDrawerCallbacks) {
            mNavigationDrawerCallbacks = (NavigationDrawerCallbacks) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mNavigationDrawerCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
        outState.putBoolean(STATE_IS_VIP_OPEN, mIsVipExpanded);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
//            inflater.inflate(R.menu.global, menu);
//            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(NavigationChoice navigationChoice);
    }
}
