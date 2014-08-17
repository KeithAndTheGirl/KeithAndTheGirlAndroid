package com.keithandthegirl.app.ui.episodesimpler;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.DatabaseHelper;
import com.keithandthegirl.app.db.model.DetailConstants;
import com.keithandthegirl.app.db.model.EpisodeConstants;
import com.keithandthegirl.app.db.model.GuestConstants;
import com.keithandthegirl.app.db.model.ImageConstants;
import com.keithandthegirl.app.db.model.ShowConstants;
import com.keithandthegirl.app.loader.AbstractAsyncTaskLoader;
import com.keithandthegirl.app.loader.WrappedLoaderCallbacks;
import com.keithandthegirl.app.loader.WrappedLoaderResult;
import com.keithandthegirl.app.ui.custom.ExpandedHeightGridView;
import com.keithandthegirl.app.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EpisodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EpisodeFragment extends Fragment implements WrappedLoaderCallbacks<EpisodeInfoHolder> {
    private static final String TAG = EpisodeFragment.class.getSimpleName();
    private static final String ARG_EPISODE_ID = "ARG_EPISODE_ID";
    private static final int VIEW_EPISODE_DETAILS = 0;
    private static final int VIEW_PROGRESS = 1;

    private long mEpisodeId;
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param episodeId episode id for the current episode.
     * @return A new instance of fragment EpisodeFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        // if this is a config change we already have episode info loaded so update UI.
        if (mEpisodeInfoHolder != null) {
            updateUI(mEpisodeInfoHolder);
        }
        return fragmentView;
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
                html = "<ul><li>" + html.replaceAll("\r\n", "</li><li>") + "</li></ul>";
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
                    episodeHolder.setEpisodeDownloaded(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_DOWNLOADED)) == 1);
                    episodeHolder.setEpisodePlayed(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_PLAYED)));
                    episodeHolder.setEpisodeLastPlayed(cursor.getLong(cursor.getColumnIndex(EpisodeConstants.FIELD_LASTPLAYED)));
                    episodeHolder.setShowNameId(cursor.getInt(cursor.getColumnIndex(EpisodeConstants.FIELD_SHOWNAMEID)));
                }
                cursor.close();

                cursor = contentResolver.query(DetailConstants.CONTENT_URI, null, DetailConstants.FIELD_SHOWID + " = ?", new String[]{String.valueOf(mEpisodeId)}, null);
                if (cursor.moveToNext()) {
                    episodeHolder.setEpisodeDetailNotes(cursor.getString(cursor.getColumnIndex(DetailConstants.FIELD_NOTES)));
                    episodeHolder.setEpisodeDetailForumUrl(cursor.getString(cursor.getColumnIndex(DetailConstants.FIELD_FORUMURL)));

                }
                cursor.close();

                int showNameId = episodeHolder.getShowNameId();
                if (showNameId > 0) {
                    cursor = contentResolver.query(ContentUris.withAppendedId(ShowConstants.CONTENT_URI, showNameId), null, null, null, null);
                    if (cursor.moveToNext()) {
                        episodeHolder.setShowName(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_NAME)));
                        episodeHolder.setShowPrefix(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_PREFIX)));
                        episodeHolder.setShowVip(cursor.getInt(cursor.getColumnIndex(ShowConstants.FIELD_VIP)) == 1 ? true : false);
                        episodeHolder.setShowCoverImageUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_COVERIMAGEURL_200)));
                        episodeHolder.setShowForumUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_FORUMURL)));
                    }
                    cursor.close();
                }

                String rawGuestsQuery =
                        "SELECT  g._id, g.realname " +
                                "FROM  guest g left join episode_guests eg on g._id = eg.showguestid " +
                                "WHERE  eg.showid = ?";
                DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                cursor = dbHelper.getReadableDatabase().rawQuery(rawGuestsQuery, new String[]{String.valueOf(mEpisodeId)});

                List<Long> guestIds = new ArrayList<Long>();
                List<String> guestNames = new ArrayList<String>();
                while (cursor.moveToNext()) {

                    guestIds.add(cursor.getLong(cursor.getColumnIndex(GuestConstants._ID)));
                    guestNames.add(cursor.getString(cursor.getColumnIndex(GuestConstants.FIELD_REALNAME)));

                }
                cursor.close();
                episodeHolder.setGuestNames(guestNames);

                String RAW_GUESTS_QUERY =
                        "SELECT g._id, g.pictureurl " +
                                "FROM  guest g left join episode_guests eg on g._id = eg.showguestid " +
                                "WHERE  eg.showid = ?";
                List<String> guestImages = new ArrayList<String>();
                dbHelper = new DatabaseHelper(getActivity());
                cursor = dbHelper.getReadableDatabase().rawQuery(RAW_GUESTS_QUERY, new String[]{String.valueOf(mEpisodeId)});
                while (cursor.moveToNext()) {
                    guestImages.add(cursor.getString(cursor.getColumnIndex(GuestConstants.FIELD_PICTUREURL)));
                }
                episodeHolder.setEpisodeGuestImages(guestImages);
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
        }
    }

    @Override
    public void onLoaderReset(final Loader<WrappedLoaderResult<EpisodeInfoHolder>> loader) {
    }
}