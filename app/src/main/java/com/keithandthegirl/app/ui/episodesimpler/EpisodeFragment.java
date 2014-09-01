package com.keithandthegirl.app.ui.episodesimpler;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.DetailConstants;
import com.keithandthegirl.app.db.model.EndpointConstants;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.ImageConstants;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.db.model.WorkItemConstants;
import com.keithandthegirl.app.loader.AbstractAsyncTaskLoader;
import com.keithandthegirl.app.loader.WrappedLoaderCallbacks;
import com.keithandthegirl.app.loader.WrappedLoaderResult;
import com.keithandthegirl.app.sync.SyncAdapter;
import com.keithandthegirl.app.ui.custom.ExpandedHeightGridView;
import com.keithandthegirl.app.utils.StringUtils;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EpisodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EpisodeFragment extends Fragment implements WrappedLoaderCallbacks<EpisodeInfoHolder>, AdapterView.OnItemClickListener {
    private static final String TAG = EpisodeFragment.class.getSimpleName();
    private static final String ARG_EPISODE_ID = "ARG_EPISODE_ID";
    private static final int VIEW_EPISODE_DETAILS = 0;
    private static final int VIEW_PROGRESS = 1;

    private long mEpisodeId;
    private EpisodeEventListener mEpisodeEventListener;
    private EpisodeInfoHolder mEpisodeInfoHolder;
    private List<String> mEpisodeGuestImagesList;
    private EpisodeGuestImageAdapter mEpisodeGuestImageAdapter;
    private List<String> mEpisodeImagesList;
    private EpisodeImageAdapter mEpisodeImageAdapter;

    private ViewSwitcher mMainViewSwitcher;
    private ImageView mEpisodeHeaderBackgroundImageView;
    private TextView mEpisodeDateTextView;
    private TextView mEpisodeNumberTextView;
    private TextView mEpisodeTitleTextView;
    private TextView mEpisodeGuestsTextView;
    private ExpandedHeightGridView mEpisodeGuestImagesGridView;
    private ExpandedHeightGridView mEpisodeImagesGridView;
    private WebView mEpisodeShowNotesWebView;
    private View mEpisodeDetailsLayout;
    private View mEpisodeGuestsLayout;
    private View mEpisodeImagesLayout;

    private MenuItem mDownloadMenuItem, mDeleteMenuItem;

    private DownloadManager mDownloadManager;

    private SyncCompleteReceiver mSyncCompleteReceiver = new SyncCompleteReceiver();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param episodeId episode id for the current episode.
     * @return A new instance of fragment EpisodeFragment.
     */
    public static EpisodeFragment newInstance(long episodeId) {
        EpisodeFragment fragment = new EpisodeFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_EPISODE_ID, episodeId);
        fragment.setArguments(args);
        return fragment;
    }

    public EpisodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEpisodeId = getArguments().getLong(ARG_EPISODE_ID);
        }

        mDownloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        mEpisodeGuestImagesList = new ArrayList<String>();
        mEpisodeGuestImageAdapter = new EpisodeGuestImageAdapter(getActivity(), mEpisodeGuestImagesList);

        mEpisodeImagesList = new ArrayList<String>();
        mEpisodeImageAdapter = new EpisodeImageAdapter(getActivity(), mEpisodeImagesList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_episodesimpler, container, false);

        mMainViewSwitcher = (ViewSwitcher) fragmentView;
        mMainViewSwitcher.setDisplayedChild(VIEW_PROGRESS);

        mEpisodeDetailsLayout = fragmentView.findViewById(R.id.episodeDetailsLayout);
        mEpisodeGuestsLayout = fragmentView.findViewById(R.id.guestsLayout);
        mEpisodeImagesLayout = fragmentView.findViewById(R.id.episodeImagesLayout);

        mEpisodeHeaderBackgroundImageView = (ImageView) fragmentView.findViewById(R.id.episodeHeaderBackgroundImageView);
        mEpisodeDateTextView = (TextView) fragmentView.findViewById(R.id.episodeDateTextView);
        mEpisodeNumberTextView = (TextView) fragmentView.findViewById(R.id.episodeNumberTextView);
        mEpisodeTitleTextView = (TextView) fragmentView.findViewById(R.id.episodeTitleTextView);
        mEpisodeGuestsTextView = (TextView) fragmentView.findViewById(R.id.episodeGuestsTextView);
        mEpisodeGuestImagesGridView = (ExpandedHeightGridView) fragmentView.findViewById(R.id.episodeGuestImagesGridView);
        mEpisodeGuestImagesGridView.setAdapter(mEpisodeGuestImageAdapter);

        mEpisodeShowNotesWebView = (WebView) fragmentView.findViewById(R.id.episodeShowNotesWebView);
        mEpisodeImagesGridView = (ExpandedHeightGridView) fragmentView.findViewById(R.id.episodeImagesGridView);
        mEpisodeImagesGridView.setAdapter(mEpisodeImageAdapter);
        mEpisodeImagesGridView.setOnItemClickListener(this);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        // only load the data once and set retain instance
        getLoaderManager().initLoader(1, null, this);
//        setRetainInstance(true);

        // if this is a config change we already have episode info loaded so update UI.
        if (mEpisodeInfoHolder != null) {
            updateUI(mEpisodeInfoHolder);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter syncCompleteIntentFilter = new IntentFilter(SyncAdapter.COMPLETE_ACTION);
        getActivity().registerReceiver(mSyncCompleteReceiver, syncCompleteIntentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();

        if (null != mSyncCompleteReceiver) {
            getActivity().unregisterReceiver(mSyncCompleteReceiver);
        }

    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (activity instanceof EpisodeEventListener) {
            mEpisodeEventListener = (EpisodeEventListener) activity;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.episode, menu);

        mDownloadMenuItem = menu.findItem( R.id.action_download );
        mDeleteMenuItem = menu.findItem( R.id.action_delete );

        swapMenuItems();

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch( item.getItemId() ) {

            case R.id.action_download :
                queueDownload();
                break;

            case R.id.action_delete :
                deleteEpisode();
                break;

            case R.id.action_settings :
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void swapMenuItems() {

        if( null == mDownloadMenuItem || null == mDeleteMenuItem ) {
            return;
        }

        if( null == mEpisodeInfoHolder ) {

            mDownloadMenuItem.setVisible( false );
            mDownloadMenuItem.setEnabled( false );
            mDeleteMenuItem.setVisible( false );
            mDeleteMenuItem.setEnabled( false );

            return;
        }

        if( !mEpisodeInfoHolder.isEpisodePublic() ) {
            return;
        }

        if( mEpisodeInfoHolder.isEpisodeDownloaded() ) {

            mDownloadMenuItem.setVisible( false );
            mDownloadMenuItem.setEnabled( false );
            mDeleteMenuItem.setVisible( true );
            mDeleteMenuItem.setEnabled( true );

            return;
        } else {

            if (mEpisodeInfoHolder.getEpisodeDownloadId() != -1) {

                mDownloadMenuItem.setVisible(false);
                mDownloadMenuItem.setEnabled(false);
                mDeleteMenuItem.setVisible(true);
                mDeleteMenuItem.setEnabled(true);

                return;
            } else {

                mDownloadMenuItem.setVisible(true);
                mDownloadMenuItem.setEnabled(true);
                mDeleteMenuItem.setVisible(false);
                mDeleteMenuItem.setEnabled(false);

                return;
            }

        }

    }

    private boolean isDownloading() {

        if( null == mEpisodeInfoHolder ) {
            return false;
        }

        boolean isDownloading = false;
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(
                DownloadManager.STATUS_PAUSED|
                        DownloadManager.STATUS_PENDING|
                        DownloadManager.STATUS_RUNNING);
        Cursor cur = mDownloadManager.query(query);
        int col = cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
        for(cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {

            if( cur.getString(col).indexOf( mEpisodeInfoHolder.getEpisodeFilename() ) != -1 ) {

                switch (cur.getInt(cur.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                    case DownloadManager.STATUS_FAILED:
                        isDownloading = false;
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        isDownloading = false;
                        break;
                    case DownloadManager.STATUS_PENDING:
                        isDownloading = false;
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        isDownloading = true;
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        isDownloading = false;
                        break;
                }

            }
        }
        cur.close();

        return isDownloading;
    }

    private void queueDownload() {

        if( null == mEpisodeInfoHolder ) {
            return;
        }

        if( !isDownloading() ) {

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mEpisodeInfoHolder.getEpisodeFileUrl()));

            // only download via Any Newtwork Connection
            // TODO: Setup preferences to allow user to decide if Mobile or WIFI networks should be used for downloads
            //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            request.setTitle(mEpisodeInfoHolder.getShowPrefix() + ":" + mEpisodeInfoHolder.getEpisodeNumber());
            request.setDescription(mEpisodeInfoHolder.getEpisodeTitle());

            // show download status in notification bar
            request.setVisibleInDownloadsUi(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(getActivity(), null, mEpisodeInfoHolder.getEpisodeFilename());
            request.setMimeType( null );

            // enqueue this request
            long downloadId = mDownloadManager.enqueue(request);

            if (downloadId > 0) {

                mEpisodeInfoHolder.setEpisodeDownloadId(downloadId);
                mEpisodeInfoHolder.setEpisodeDownloaded(false);

                swapMenuItems();

                ContentValues values = new ContentValues();
                values.put(EpisodeConstants.FIELD_DOWNLOAD_ID, downloadId);
                values.put(EpisodeConstants.FIELD_DOWNLOADED, 0);
                getActivity().getContentResolver().update(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, mEpisodeId), values, null, null);
            }

        }

    }

    private void deleteEpisode() {

        if( null == mEpisodeInfoHolder ) {
            return;
        }

        if( isDownloading() ) {

            mDownloadManager.remove(mEpisodeInfoHolder.getEpisodeDownloadId());

        } else {

            File externalFile = new File(getActivity().getExternalFilesDir(null), mEpisodeInfoHolder.getEpisodeFilename());
            if (externalFile.exists()) {
                boolean deleted = externalFile.delete();
//                if (deleted) {
//                    Log.i(TAG, "deleteEpisode : externalFile deleted!");
//
//                }
            }

        }

        mEpisodeInfoHolder.setEpisodeDownloadId(-1);
        mEpisodeInfoHolder.setEpisodeDownloaded(false);

        ContentValues values = new ContentValues();
        values.put(EpisodeConstants.FIELD_DOWNLOAD_ID, -1);
        values.put(EpisodeConstants.FIELD_DOWNLOADED, 0);
        getActivity().getContentResolver().update(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, mEpisodeId), values, null, null);

        swapMenuItems();

    }

    private void updateUI(EpisodeInfoHolder episodeHolder) {
        if (episodeHolder == null) {
            return;
        } // early out if we haven't set data yet

        Picasso.with(getActivity()).load(episodeHolder.getShowCoverImageUrl()).into(mEpisodeHeaderBackgroundImageView);
        mEpisodeDateTextView.setText(episodeHolder.getEpisodePosted());
        mEpisodeNumberTextView.setText(String.valueOf(episodeHolder.getEpisodeNumber()));
        mEpisodeTitleTextView.setText(episodeHolder.getEpisodeTitle());

        if (!StringUtils.isNullOrEmpty(episodeHolder.getGuestNames())) {
            mEpisodeGuestsLayout.setVisibility(View.GONE);
        } else {
            mEpisodeGuestsLayout.setVisibility(View.VISIBLE);
            mEpisodeGuestsTextView.setText(episodeHolder.getGuestNames());
        }

        if (episodeHolder.getEpisodeGuestImages().size() > 0) {
            mEpisodeImagesLayout.setVisibility(View.VISIBLE);
            mEpisodeGuestImagesList.clear();
            mEpisodeGuestImagesList.addAll(episodeHolder.getEpisodeGuestImages());
            mEpisodeGuestImageAdapter.notifyDataSetChanged();
        } else {
            mEpisodeImagesLayout.setVisibility(View.GONE);
        }

        if (episodeHolder.getEpisodeImages().size() > 0) {
            mEpisodeImagesGridView.setVisibility(View.VISIBLE);
            mEpisodeImagesList.clear();
            mEpisodeImagesList.addAll(episodeHolder.getEpisodeImages());
            mEpisodeImageAdapter.notifyDataSetChanged();
        } else {
            mEpisodeImagesGridView.setVisibility(View.GONE);
        }

        if (StringUtils.isNullOrEmpty(episodeHolder.getEpisodeDetailNotes())) {
            mEpisodeDetailsLayout.setVisibility(View.GONE);
        } else {
            mEpisodeDetailsLayout.setVisibility(View.VISIBLE);
            WebSettings settings = mEpisodeShowNotesWebView.getSettings();
            settings.setDefaultTextEncodingName("utf-8");
            String html = episodeHolder.getEpisodeDetailNotes();
            if (!StringUtils.isNullOrEmpty(html)) {
                html = "<ul><li>" + html.replaceAll("\r\n", "</li><li>") + "</li></ul></br>";
            }
            mEpisodeShowNotesWebView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
            mEpisodeShowNotesWebView.setBackgroundColor(0);
        }
        mMainViewSwitcher.setDisplayedChild(VIEW_EPISODE_DETAILS);

        swapMenuItems();
    }

    @Override
    public Loader<WrappedLoaderResult<EpisodeInfoHolder>> onCreateLoader(final int id, final Bundle args) {
        return new AbstractAsyncTaskLoader<EpisodeInfoHolder>(getActivity()) {
            @Override
            public EpisodeInfoHolder load() throws Exception {
                EpisodeInfoHolder episodeHolder = new EpisodeInfoHolder();

                ContentResolver contentResolver = getActivity().getContentResolver();
                Cursor cursor = contentResolver.query(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, mEpisodeId), null, null, null, null);
                if (cursor.moveToNext()) {
                    episodeHolder.setEpisodeNumber(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_NUMBER)));
                    episodeHolder.setEpisodeTitle(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_TITLE)));
                    episodeHolder.setEpisodePreviewUrl(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_PREVIEWURL)));
                    episodeHolder.setEpisodeFileUrl(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_FILEURL)));
                    episodeHolder.setEpisodeFilename(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_FILENAME)));
                    episodeHolder.setEpisodeLength(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_LENGTH)));
                    episodeHolder.setEpisodeFileSize(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_FILESIZE)));
                    episodeHolder.setEpisodeType(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_TYPE)));
                    episodeHolder.setEpisodePublic(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_PUBLIC)) == 1);
                    episodeHolder.setEpisodePosted(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_POSTED)));
                    episodeHolder.setEpisodeDownloadId(cursor.getLong(cursor.getColumnIndex(EpisodeConstants.FIELD_DOWNLOAD_ID)));
                    episodeHolder.setEpisodeDownloaded(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_DOWNLOADED)) == 1);
                    episodeHolder.setEpisodePlayed(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_PLAYED)));
                    episodeHolder.setEpisodeLastPlayed(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_LASTPLAYED)));
                    episodeHolder.setShowNameId(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_SHOWNAMEID)));
                    episodeHolder.setEpisodeDetailNotes(cursor.getString(cursor.getColumnIndex(DetailConstants.TABLE_NAME + "_" + DetailConstants.FIELD_NOTES)));
                    episodeHolder.setEpisodeDetailForumUrl(cursor.getString(cursor.getColumnIndex(DetailConstants.TABLE_NAME + "_" + DetailConstants.FIELD_FORUMURL)));
                    episodeHolder.setShowName(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_NAME)));
                    episodeHolder.setShowPrefix(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_PREFIX)));
                    episodeHolder.setShowVip(cursor.getInt(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_VIP)) == 1 ? true : false);
                    episodeHolder.setShowCoverImageUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_COVERIMAGEURL_200)));
                    episodeHolder.setShowForumUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.TABLE_NAME + "_" + ShowConstants.FIELD_FORUMURL)));
                    episodeHolder.setGuestNames(cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_GUEST_NAMES)));

                    String guestImages = cursor.getString(cursor.getColumnIndex(EpisodeConstants.FIELD_GUEST_IMAGES));
                    if(null != guestImages && !"".equals(guestImages)) {
                       String[] images = guestImages.split(",");
                       episodeHolder.setEpisodeGuestImages(Arrays.asList(images));
                    } else {
                        episodeHolder.setEpisodeGuestImages(Collections.EMPTY_LIST);
                    }

                }
                cursor.close();

                String[] projection = {ImageConstants._ID, ImageConstants.FIELD_MEDIAURL};
                String selection = ImageConstants.FIELD_SHOWID + " = ?";
                String[] selectionArgs = new String[]{String.valueOf(mEpisodeId)};

                List<String> episodeImages = new ArrayList<String>();
                cursor = contentResolver.query(ImageConstants.CONTENT_URI, projection, selection, selectionArgs, ImageConstants.FIELD_DISPLAY_ORDER);
                while (cursor.moveToNext()) {
                    String mediaUrl = cursor.getString(cursor.getColumnIndex(ImageConstants.FIELD_MEDIAURL));
                    episodeImages.add(mediaUrl);
                }
                cursor.close();
                episodeHolder.setEpisodeImages(episodeImages);

                return episodeHolder;
            }
        };
    }

    @Override
    public void onLoadFinished(final Loader<WrappedLoaderResult<EpisodeInfoHolder>> loader, final WrappedLoaderResult<EpisodeInfoHolder> wrappedData) {
        if (wrappedData.hasException()) {
            // TODO what to display when we failed getting stuff from DB?
        } else {
            mEpisodeInfoHolder = wrappedData.getWrappedData();
            updateUI(mEpisodeInfoHolder);
            if (mEpisodeEventListener != null) {

                mEpisodeEventListener.onEpisodeLoaded(mEpisodeInfoHolder);
            }

            if(null == mEpisodeInfoHolder.getEpisodeDetailNotes() || "".equals(mEpisodeInfoHolder.getEpisodeDetailNotes())) {
                scheduleWorkItem(mEpisodeInfoHolder.getShowName());
            }
        }
    }

    @Override
    public void onLoaderReset(final Loader<WrappedLoaderResult<EpisodeInfoHolder>> loader) { }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        if (mEpisodeEventListener != null) {
            mEpisodeEventListener.onShowImageClicked(position, mEpisodeInfoHolder.getEpisodeImages());
        }
    }

    public interface EpisodeEventListener {
        void onEpisodeLoaded(EpisodeInfoHolder episodeInfoHolder);
        void onShowImageClicked(int position, List<String> imageUrls);
    }

    private void scheduleWorkItem( String showName ) {

        ContentValues values = new ContentValues();
        values.put( WorkItemConstants.FIELD_NAME, showName + " " + mEpisodeId + " details" );
        values.put( WorkItemConstants.FIELD_FREQUENCY, WorkItemConstants.Frequency.ON_DEMAND.name() );
        values.put( WorkItemConstants.FIELD_DOWNLOAD, WorkItemConstants.Download.JSON.name() );
        values.put( WorkItemConstants.FIELD_ENDPOINT, EndpointConstants.Type.DETAILS.name() );
        values.put( WorkItemConstants.FIELD_ADDRESS, EndpointConstants.DETAILS );
        values.put( WorkItemConstants.FIELD_PARAMETERS, "?showid=" + mEpisodeId );
        values.put( WorkItemConstants.FIELD_STATUS, WorkItemConstants.Status.NEVER.name() );
        values.put( WorkItemConstants.FIELD_LAST_MODIFIED_DATE, new DateTime( DateTimeZone.UTC ).getMillis() );

        Cursor cursor = getActivity().getContentResolver().query( WorkItemConstants.CONTENT_URI, null, WorkItemConstants.FIELD_ADDRESS + " = ? AND " + WorkItemConstants.FIELD_PARAMETERS + " = ?", new String[] { EndpointConstants.DETAILS, "?showid=" + mEpisodeId }, null );
        if( cursor.moveToNext() ) {

            long id = cursor.getLong( cursor.getColumnIndex( WorkItemConstants._ID) );
            getActivity().getContentResolver().update( ContentUris.withAppendedId( WorkItemConstants.CONTENT_URI, id ), values, null, null );

        } else {

            getActivity().getContentResolver().insert( WorkItemConstants.CONTENT_URI, values );

        }
        cursor.close();
    }

    private class SyncCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // when we receive a syc complete action reset the loader so it can refresh the content
            if (intent.getAction().equals(SyncAdapter.COMPLETE_ACTION)) {
                getLoaderManager().restartLoader(1, null, EpisodeFragment.this);
            }
        }
    }

}