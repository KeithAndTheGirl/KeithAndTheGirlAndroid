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
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.keithandthegirl.app.db.DatabaseHelper;
import com.keithandthegirl.app.db.model.DetailConstants;
import com.keithandthegirl.app.db.model.EndpointConstants;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.GuestConstants;
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

    private Button mEpisodeDownloadButton;

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
        // only load the data once and set retain instance
        getLoaderManager().initLoader(1, null, this);
        setRetainInstance(true);

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

        // if this is a config change we already have episode info loaded so update UI.
        if (mEpisodeInfoHolder != null) {
            updateUI(mEpisodeInfoHolder);
        }

        mEpisodeDownloadButton = (Button) fragmentView.findViewById( R.id.episode_download );
        mEpisodeDownloadButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {

//                final File file = new File(Environment.getExternalStoragePublicDirectory(null), mEpisodeInfoHolder.getEpisodeFilename());
                DownloadManager mgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                boolean isDownloading = false;
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterByStatus(
                        DownloadManager.STATUS_PAUSED|
                                DownloadManager.STATUS_PENDING|
                                DownloadManager.STATUS_RUNNING|
                                DownloadManager.STATUS_SUCCESSFUL);
                Cursor cur = mgr.query(query);
                int col = cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                for(cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    isDownloading = isDownloading || (mEpisodeInfoHolder.getEpisodeFilename() == cur.getString(col));
                }
                cur.close();

                if( !isDownloading ) {

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

                    // enqueue this request
                    DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                    long downloadId = downloadManager.enqueue(request);

                    if (downloadId > 0) {
                        mEpisodeInfoHolder.setEpisodeDownloadId(downloadId);
                        ContentValues values = new ContentValues();
                        values.put(EpisodeConstants.FIELD_DOWNLOAD_ID, downloadId);
                        values.put(EpisodeConstants.FIELD_DOWNLOADED, 0);
                        getActivity().getContentResolver().update(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, mEpisodeId), values, null, null);
                    }

                }
            }

        });

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter syncCompleteIntentFilter = new IntentFilter(SyncAdapter.COMPLETE_ACTION);
        getActivity().registerReceiver(mSyncCompleteReceiver, syncCompleteIntentFilter);

        getActivity().registerReceiver(mDownloadCompleteReceiver, mDownloadCompleteIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mSyncCompleteReceiver) {
            getActivity().unregisterReceiver(mSyncCompleteReceiver);
        }

        if (null != mDownloadCompleteReceiver) {
            getActivity().unregisterReceiver(mDownloadCompleteReceiver);
        }
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (activity instanceof EpisodeEventListener) {
            mEpisodeEventListener = (EpisodeEventListener) activity;
        }
    }

    private void updateUI(EpisodeInfoHolder episodeHolder) {
        if (episodeHolder == null) {
            return;
        } // early out if we haven't set data yet

        Picasso.with(getActivity()).load(episodeHolder.getShowCoverImageUrl()).into(mEpisodeHeaderBackgroundImageView);
        mEpisodeDateTextView.setText(episodeHolder.getEpisodePosted());
        mEpisodeNumberTextView.setText(String.valueOf(episodeHolder.getEpisodeNumber()));
        mEpisodeTitleTextView.setText(episodeHolder.getEpisodeTitle());

        if (episodeHolder.getGuestNames().size() == 0) {
            mEpisodeGuestsLayout.setVisibility(View.GONE);
        } else {
            mEpisodeGuestsLayout.setVisibility(View.VISIBLE);
            mEpisodeGuestsTextView.setText(StringUtils.getCommaSeparatedString(episodeHolder.getGuestNames(), true));
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
                }
                cursor.close();

                String rawGuestsQuery =
                        "SELECT  g._id, g.realname, g.pictureurl " +
                                "FROM  guest g left join episode_guests eg on g._id = eg.showguestid " +
                                "WHERE  eg.showid = ?";
                DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                cursor = dbHelper.getReadableDatabase().rawQuery(rawGuestsQuery, new String[]{String.valueOf(mEpisodeId)});

                List<Long> guestIds = new ArrayList<Long>();
                List<String> guestNames = new ArrayList<String>();
                List<String> guestImages = new ArrayList<String>();
                while (cursor.moveToNext()) {

                    guestIds.add(cursor.getLong(cursor.getColumnIndex(GuestConstants._ID)));
                    guestNames.add(cursor.getString(cursor.getColumnIndex(GuestConstants.FIELD_REALNAME)));
                    guestImages.add(cursor.getString(cursor.getColumnIndex(GuestConstants.FIELD_PICTUREURL)));

                }
                cursor.close();
                episodeHolder.setGuestNames(guestNames);
                episodeHolder.setEpisodeGuestImages(guestImages);

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
                mEpisodeEventListener.onEpisodeLoaded(mEpisodeInfoHolder.getEpisodeFileUrl(), mEpisodeInfoHolder.getEpisodeLastPlayed());
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
        void onEpisodeLoaded(String episodeFileUrl, int lastPlayedPosition);
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

        Log.v( TAG, "scheduleWorkItem : exit" );
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

    private String mDownloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    private IntentFilter mDownloadCompleteIntentFilter = new IntentFilter(mDownloadCompleteIntentName);
    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if(id != mEpisodeInfoHolder.getEpisodeDownloadId()) {
                return;
            }

            mEpisodeInfoHolder.setEpisodeDownloadId(-1);
            ContentValues values = new ContentValues();
            values.put(EpisodeConstants.FIELD_DOWNLOAD_ID, -1);
            values.put(EpisodeConstants.FIELD_DOWNLOADED, 1);
            getActivity().getContentResolver().update(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, mEpisodeId), values, null, null);

            Log.i( TAG, "Episode Downloaded!" );
        }

    };

}