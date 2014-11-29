package com.keithandthegirl.app.ui.shows;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.EpisodeInfoHolder;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.db.model.ShowInfoHolder;
import com.keithandthegirl.app.sync.EpisodeListAsyncTask;
import com.keithandthegirl.app.sync.SyncAdapter;
import com.keithandthegirl.app.ui.custom.SwipeRefreshListFragment;
import com.keithandthegirl.app.ui.episode.EpisodeActivity;
import com.keithandthegirl.app.ui.utils.EndlessScrollListener;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

/**
 *
 * Created by dmfrey on 3/30/14.
 */
public class ShowFragment extends SwipeRefreshListFragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ShowFragment.class.getSimpleName();

    public static final String SHOW_NAME_ID_KEY = "showNameId";

    private OnShowFragmentListener mListener;
    private View mHeaderView;
    private EpisodeCursorAdapter mAdapter;
    private SyncCompleteReceiver mSyncCompleteReceiver = new SyncCompleteReceiver();
    private EpisodeListSyncCompleteReceiver mEpisodeListSyncCompleteReceiver = new EpisodeListSyncCompleteReceiver();
    private long mShowNameId;

    private ImageView mCoverImageView;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;

    private ShowInfoHolder mShowHolder;

    private boolean mMobileConnected, mWifiConnected;

    /**
     * Returns a new instance of this fragment for the given show id.
     */
    public static ShowFragment newInstance(long showNameId) {
        ShowFragment fragment = new ShowFragment();

        Bundle args = new Bundle();
        args.putLong(SHOW_NAME_ID_KEY, showNameId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getArguments()) {
            mShowNameId = getArguments().getLong(SHOW_NAME_ID_KEY);

            mShowHolder = ShowInfoHolder.loadShow( getActivity(), mShowNameId );
        }
        mAdapter = new EpisodeCursorAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        mHeaderView = inflater.inflate(R.layout.listview_header_show, null, false);
        mCoverImageView = (ImageView) mHeaderView.findViewById(R.id.show_coverimage);
        mTitleTextView = (TextView) mHeaderView.findViewById(R.id.show_title);
        mDescriptionTextView = (TextView) mHeaderView.findViewById(R.id.show_description);

        setOnRefreshListener(this);
        setColorScheme(R.color.katg_refresh_1,
                R.color.katg_refresh_2,
                R.color.katg_refresh_3,
                R.color.katg_refresh_4);
        return rootView;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().addHeaderView(mHeaderView, null, false);
        getListView().setOnScrollListener( new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.i( TAG, "onLoadMore : enter" );

                loadMoreItems();

                Log.i( TAG, "onLoadMore : exit" );
            }
        });
        setListAdapter(mAdapter);

        // Setting the empty text will turn the fragment view into a blank screen with the text centered
        // it also removes the view that the pull to refresh is attached to so you can't get it to try
        // and refresh. Leaving it out gives you a view with the header and an empty view that you can
        // at least pull down and attempt a refresh. But this is the right place to put the call.
        //setEmptyText("empty text");

        // Set the progress to visible on init by hiding list. As soon as you load data set it to true
        setListShown(false);
        Bundle args = new Bundle();
        args.putLong(SHOW_NAME_ID_KEY, mShowNameId);
        getLoaderManager().initLoader(0, args, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnShowFragmentListener ) {
            mListener = (OnShowFragmentListener) activity;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter syncCompleteIntentFilter = new IntentFilter(SyncAdapter.COMPLETE_ACTION);
        getActivity().registerReceiver(mSyncCompleteReceiver, syncCompleteIntentFilter);

        IntentFilter episodeListSyncCompleteIntentFilter = new IntentFilter(EpisodeListAsyncTask.COMPLETE_ACTION);
        getActivity().registerReceiver(mEpisodeListSyncCompleteReceiver, episodeListSyncCompleteIntentFilter);

        updateConnectedFlags();

    }

    @Override
    public void onPause() {
        super.onPause();

        if (null != mSyncCompleteReceiver) {
            getActivity().unregisterReceiver(mSyncCompleteReceiver);
        }

        if (null != mEpisodeListSyncCompleteReceiver) {
            getActivity().unregisterReceiver(mEpisodeListSyncCompleteReceiver);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // set list adapter to null so that the header can be recreated in onCrateView
        setListAdapter(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateHeader(mShowNameId);
    }

    @Override
    public void onRefresh() {

        updateConnectedFlags();

        if( !mWifiConnected && !mMobileConnected ) {
            return;
        }

        // a swipe occurred and we need to start refreshing then start the sync adapter
        // we will get a broadcast event when the sync adapter is done.
        setRefreshing( true );

        new EpisodeListAsyncTask( getActivity(), (int) mShowNameId, -1, -1, 50, true ).execute();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        String[] projection = null;
        String selection = EpisodeConstants.FIELD_SHOWNAMEID + "=?";

        mShowNameId = args.getLong(SHOW_NAME_ID_KEY);
        String[] selectionArgs = new String[]{String.valueOf(mShowNameId)};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), EpisodeConstants.CONTENT_URI, projection, selection, selectionArgs, EpisodeConstants.FIELD_NUMBER + " DESC");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        setListShown(true);
        setRefreshing(false);
        mAdapter.swapCursor(cursor);
        getListView().setFastScrollEnabled(true);

        Log.i( TAG, "onLoadFinished : cursor count=" + cursor.getCount() );
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

//        if (mListener != null) {
//            mListener.onShowSelected(mShowNameId, id);
//        }
        Intent i = new Intent(getActivity(), EpisodeActivity.class);
        i.putExtra(EpisodeActivity.EPISODE_KEY, id);
        startActivity(i);
    }

    public void loadMoreItems() {
        Log.d( TAG, "loadMoreItems : enter" );
        Log.d( TAG, "loadMoreItems : mShowHolder=" + mShowHolder.toString() );

        updateConnectedFlags();

        if( !mWifiConnected && !mMobileConnected ) {
            return;
        }

        int episodeCount = getListView().getCount();
        Log.d( TAG, "loadMoreItems : episodeCount=" + episodeCount + ", show count=" + mShowHolder.getEpisodeNumberMax() );

//        if( episodeCount < mShowHolder.getEpisodeNumberMax() ) {

            int lastIndex = getListView().getCount() - 1;
            long lastId = getListAdapter().getItemId( lastIndex );
            Log.d( TAG, "loadMoreItems : lastIndex=" + lastIndex + ", lastId=" + lastId );

            EpisodeInfoHolder episodeHolder = EpisodeInfoHolder.loadEpisode( getActivity(), lastId );
            int number = episodeHolder.getEpisodeNumber() - 50;
            if( number < 1 ) {
                number = 1;
            }

            new EpisodeListAsyncTask( getActivity(), (int) mShowNameId, -1, number, 50, false ).execute();

            setRefreshing( true );

//        }

        Log.d( TAG, "loadMoreItems : exit" );
    }

    public void updateHeader(long showNameId) {
        String[] projection = new String[]{ShowConstants._ID, ShowConstants.FIELD_NAME, ShowConstants.FIELD_DESCRIPTION, ShowConstants.FIELD_COVERIMAGEURL_200};

        Cursor cursor = getActivity().getContentResolver().query(ContentUris.withAppendedId(ShowConstants.CONTENT_URI, showNameId), projection, null, null, null);
        if (cursor.moveToNext()) {

            String coverUrl = cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_COVERIMAGEURL_200));

            mTitleTextView.setText(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_NAME)));
            mDescriptionTextView.setText(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_DESCRIPTION)));
            Picasso.with(getActivity()).load(coverUrl).fit().centerCrop().into(mCoverImageView);
        }
        cursor.close();
    }

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    private void updateConnectedFlags() {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if( null != activeInfo && activeInfo.isConnected() ) {
            mWifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mMobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            mWifiConnected = false;
            mMobileConnected = false;
        }

    }

    private static class ViewHolder {
        TextView number;
        TextView showDate;
        TextView title;
//        ImageView details;
        TextView played;
        TextView downloaded;
        TextView guestsTextView;
        TextView duration;
        public View minutesLayout;

        ViewHolder() {
        }
    }

    private class EpisodeCursorAdapter extends CursorAdapter {
        String mEpisodesLabel;
        private LayoutInflater mInflater;

        public EpisodeCursorAdapter(Context context) {
            super(context, null, false);

            mInflater = LayoutInflater.from(context);
            mEpisodesLabel = getString(R.string.episode_label);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.listview_row_show_episode, parent, false);

            ViewHolder refHolder = new ViewHolder();
            refHolder.number = (TextView) view.findViewById(R.id.episode_number);
            refHolder.showDate = (TextView) view.findViewById(R.id.episode_date);
            refHolder.title = (TextView) view.findViewById(R.id.episode_title);
//            refHolder.details = (ImageView) view.findViewById(R.id.episode_details);
            refHolder.played = (TextView) view.findViewById(R.id.episode_played);
            refHolder.downloaded = (TextView) view.findViewById(R.id.episode_downloaded);
            refHolder.guestsTextView = (TextView) view.findViewById(R.id.guestsTextView);
            refHolder.duration = (TextView) view.findViewById(R.id.episode_duration);
            refHolder.minutesLayout = view.findViewById(R.id.minutesLayout);

            view.setTag(refHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder mHolder = (ViewHolder) view.getTag();

            long id = cursor.getLong(cursor.getColumnIndex(EpisodeConstants._ID));
            int length = cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_LENGTH));
            int lastPlayed = cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_LASTPLAYED));
            long instant = cursor.getLong(cursor.getColumnIndex(EpisodeConstants.FIELD_TIMESTAMP));
            int downloaded = cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_DOWNLOADED));
            String guestNames = cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_GUEST_NAMES));

            mHolder.number.setText(mEpisodesLabel + " " + cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_NUMBER)));
            mHolder.showDate.setText(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_POSTED)));
            mHolder.title.setText(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_TITLE)));
            if( null != guestNames ) {
                mHolder.guestsTextView.setVisibility( View.VISIBLE);
                mHolder.guestsTextView.setText(guestNames);
            } else {
                mHolder.guestsTextView.setVisibility( View.GONE);
            }
            int minutes = length/60;
            if (minutes > 0) {
                mHolder.minutesLayout.setVisibility(View.VISIBLE);
                mHolder.duration.setText(String.valueOf(minutes));
            } else {
                mHolder.minutesLayout.setVisibility(View.GONE);
            }

            if( downloaded == 1) {
                mHolder.downloaded.setText("Downloaded");
            } else {
                mHolder.downloaded.setText("Not Downloaded");
            }

            if( lastPlayed > 0 ) {
                // multiply length by 1000, length is reported in seconds from the server
                double percent = ( (double)lastPlayed / (double)(length * 1000) );

                mHolder.played.setText(MessageFormat.format("{0,number,percent}", percent) + " Played");
            } else {
                mHolder.played.setText("Not Played");
            }
        }
    }

    private class SyncCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // when we receive a syc complete action reset the loader so it can refresh the content
            if (intent.getAction().equals(SyncAdapter.COMPLETE_ACTION)) {
                Bundle args = new Bundle();
                args.putLong(SHOW_NAME_ID_KEY, mShowNameId);
                getLoaderManager().restartLoader(0, args, ShowFragment.this);
            }
        }

    }

    private class EpisodeListSyncCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // when we receive a syc complete action reset the loader so it can refresh the content
            if (intent.getAction().equals(EpisodeListAsyncTask.COMPLETE_ACTION)) {
                setRefreshing( false );
            }
        }
    }

    public interface OnShowFragmentListener {
        public void onShowSelected(long showId, long episodeId);
    }

}
