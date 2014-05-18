package com.keithandthegirl.app.ui;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.Toast;

import com.keithandthegirl.app.MainApplication;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.Detail;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.Show;
import com.keithandthegirl.app.db.schedule.KatgAlarmReceiver;
import com.keithandthegirl.app.ui.shows.EpisodeDetailsFragment;
import com.keithandthegirl.app.ui.shows.EpisodeFragment;
import com.keithandthegirl.app.ui.shows.EpisodeGuestImagesFragment;
import com.keithandthegirl.app.ui.shows.EpisodeGuestsFragment;
import com.keithandthegirl.app.ui.shows.EpisodeHeaderFragment;
import com.keithandthegirl.app.ui.shows.EpisodeImagesFragment;

import org.joda.time.DateTime;

public class EpisodeActivity extends AbstractBaseActivity {

    private static final String TAG = EpisodeActivity.class.getSimpleName();

    private static final String TAG_EPISODE_HEADER_FRAGMENT = "episode_header_fragment";
    private static final String TAG_EPISODE_GUESTS_FRAGMENT = "episode_guests_fragment";
    private static final String TAG_EPISODE_GUEST_IMAGES_FRAGMENT = "episode_guest_images_fragment";
    private static final String TAG_EPISODE_DETAILS_FRAGMENT = "episode_details_fragment";
    private static final String TAG_EPISODE_IMAGES_FRAGMENT = "episode_images_fragment";

    public static final String EPISODE_KEY = "episode_id";
    public static final String EPISODE_NUMBER_KEY = "episode_number";
    public static final String EPISODE_TITLE_KEY = "episode_title";
    public static final String EPISODE_PREVIEW_URL_KEY = "episode_preview_url";
    public static final String EPISODE_FILE_URL_KEY = "episode_file_url";
    public static final String EPISODE_FILENAME_KEY = "episode_filename";
    public static final String EPISODE_LENGTH_KEY = "episode_length";
    public static final String EPISODE_FILE_SIZE_KEY = "episode_file_size";
    public static final String EPISODE_TYPE_KEY = "episode_type";
    public static final String EPISODE_PUBLIC_KEY = "episode_public";
    public static final String EPISODE_POSTED_KEY = "episode_posted";
    public static final String EPISODE_DOWNLOADED_KEY = "episode_downloaded";
    public static final String EPISODE_PLAYED_KEY = "episode_played";
    public static final String EPISODE_LAST_PLAYED_KEY = "episode_last_played";

    public static final String EPISODE_DETAIL_NOTES_KEY = "episode_detail_notes";
    public static final String EPISODE_DETAIL_FORUM_URL_KEY = "episode_detail_forum_url";

    public static final String SHOW_KEY = "show_id";
    public static final String SHOW_NAME_KEY = "show_name";
    public static final String SHOW_PREFIX_KEY = "show_prefix";
    public static final String SHOW_VIP_KEY = "show_VIP";
    public static final String SHOW_COVER_IMAGE_URL_KEY = "show_cover_image_url";
    public static final String SHOW_FORUM_URL_KEY = "show_forum_url";

    private long mEpisodeId, mEpisodePosted, mEpisodeLastPlayed;
    private int mEpisodeNumber, mEpisodeLength, mEpisodeFileSize, mEpisodeType, mEpisodePlayed, mShowNameId;
    private String mEpisodeTitle, mEpisodePreviewUrl, mEpisodeFileUrl, mEpisodeFilename, mEpisodeDetailNotes, mEpisodeDetailForumUrl, mShowName, mShowPrefix, mShowCoverImageUrl, mShowForumUrl;
    private boolean mEpisodePublic, mEpisodeDownloaded, mShowVip;

    private EpisodeHeaderFragment mEpisodeHeaderFragment;
    private EpisodeGuestsFragment mEpisodeGuestsFragment;
    private EpisodeGuestImagesFragment mEpisodeGuestImagesFragment;
    private EpisodeDetailsFragment mEpisodeDetailsFragment;
    private EpisodeImagesFragment mEpisodeImagesFragment;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        Log.d( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_episode );

        Bundle extras = getIntent().getExtras();
        if( extras.containsKey( EPISODE_KEY ) ) {
            mEpisodeId = extras.getLong( EPISODE_KEY );

            updateView();
        } else {
            Toast.makeText( this, "Episode not found!", Toast.LENGTH_LONG ).show();

            finish();
        }

        Log.d( TAG, "onCreate : exit" );
    }

    private void updateView() {
        Log.v( TAG, "updateView : enter" );

        ActionBar actionBar = getSupportActionBar();

        Cursor cursor = getContentResolver().query( ContentUris.withAppendedId( Episode.CONTENT_URI, mEpisodeId ), null, null, null, null );
        if( cursor.moveToNext() ) {

            mEpisodeNumber = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_NUMBER ) );
            mEpisodeTitle = cursor.getString( cursor.getColumnIndex( Episode.FIELD_TITLE ) );
            mEpisodePreviewUrl = cursor.getString( cursor.getColumnIndex( Episode.FIELD_PREVIEWURL ) );
            mEpisodeFileUrl = cursor.getString( cursor.getColumnIndex( Episode.FIELD_FILEURL ) );
            mEpisodeFilename = cursor.getString( cursor.getColumnIndex( Episode.FIELD_FILENAME ) );
            mEpisodeLength = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_LENGTH ) );
            mEpisodeFileSize = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_FILESIZE ) );
            mEpisodeType = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_TYPE ) );
            mEpisodePublic = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_PUBLIC ) ) == 1 ? true : false;
            mEpisodePosted = cursor.getLong( cursor.getColumnIndex( Episode.FIELD_TIMESTAMP ) );
            mEpisodeDownloaded = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_DOWNLOADED ) ) == 1 ? true : false;
            mEpisodePlayed = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_PLAYED ) );
            mEpisodeLastPlayed = cursor.getLong( cursor.getColumnIndex( Episode.FIELD_LASTPLAYED ) );

            mShowNameId = cursor.getInt( cursor.getColumnIndex( Episode.FIELD_SHOWNAMEID ) );

        }
        cursor.close();

        cursor = getContentResolver().query( Detail.CONTENT_URI, null, Detail.FIELD_SHOWID + " = ?", new String[] { String.valueOf( mEpisodeId ) }, null );
        if( cursor.moveToNext() ) {

            mEpisodeDetailNotes = cursor.getString( cursor.getColumnIndex( Detail.FIELD_NOTES ) );
            mEpisodeDetailForumUrl = cursor.getString( cursor.getColumnIndex( Detail.FIELD_FORUMURL ) );

        }
        cursor.close();

        if( mShowNameId > 0 ) {

            cursor = getContentResolver().query( ContentUris.withAppendedId( Show.CONTENT_URI, mShowNameId ), null, null, null, null );
            if( cursor.moveToNext() ) {

                mShowName = cursor.getString( cursor.getColumnIndex( Show.FIELD_NAME ) );
                mShowPrefix = cursor.getString( cursor.getColumnIndex( Show.FIELD_PREFIX ) );
                mShowVip = cursor.getInt( cursor.getColumnIndex( Show.FIELD_VIP ) ) == 1 ? true : false;
                mShowCoverImageUrl = cursor.getString( cursor.getColumnIndex( Show.FIELD_COVERIMAGEURL_200 ) );
                mShowForumUrl = cursor.getString( cursor.getColumnIndex( Show.FIELD_FORUMURL ) );

            }
            cursor.close();

        }

        actionBar.setTitle( mShowName );

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();

        mEpisodeHeaderFragment = (EpisodeHeaderFragment) fm.findFragmentByTag( TAG_EPISODE_HEADER_FRAGMENT );
        if( null == mEpisodeHeaderFragment ) {
            Log.v( TAG, "updateView : creating new episode header fragment" );

            mEpisodeHeaderFragment = new EpisodeHeaderFragment();
            Bundle episodeHeaderArgs = new Bundle();
            episodeHeaderArgs.putInt( EPISODE_NUMBER_KEY, mEpisodeNumber );
            episodeHeaderArgs.putString( EPISODE_TITLE_KEY, mEpisodeTitle );
            episodeHeaderArgs.putLong( EPISODE_POSTED_KEY, mEpisodePosted );
            episodeHeaderArgs.putString( SHOW_NAME_KEY, mShowPrefix );
            episodeHeaderArgs.putString( SHOW_COVER_IMAGE_URL_KEY, mShowCoverImageUrl );
            mEpisodeHeaderFragment.setArguments( episodeHeaderArgs );
            tx.add( R.id.episode_header, mEpisodeHeaderFragment, TAG_EPISODE_HEADER_FRAGMENT );

        }

        mEpisodeGuestsFragment = (EpisodeGuestsFragment) fm.findFragmentByTag( TAG_EPISODE_GUESTS_FRAGMENT );
        if( null == mEpisodeGuestsFragment ) {
            Log.v( TAG, "updateView : creating new episode guests fragment" );

            mEpisodeGuestsFragment = new EpisodeGuestsFragment();
            Bundle episodeGuestsArgs = new Bundle();
            episodeGuestsArgs.putLong( EPISODE_KEY, mEpisodeId );
            mEpisodeGuestsFragment.setArguments( episodeGuestsArgs );
            tx.add(R.id.episode_guests, mEpisodeGuestsFragment, TAG_EPISODE_GUESTS_FRAGMENT);

        }

        mEpisodeGuestImagesFragment = (EpisodeGuestImagesFragment) fm.findFragmentByTag( TAG_EPISODE_GUEST_IMAGES_FRAGMENT );
        if( null == mEpisodeGuestImagesFragment ) {
            Log.v( TAG, "updateView : creating new episode guest images fragment" );

            mEpisodeGuestImagesFragment = new EpisodeGuestImagesFragment();
            Bundle episodeGuestsArgs = new Bundle();
            episodeGuestsArgs.putLong( EPISODE_KEY, mEpisodeId );
            mEpisodeGuestImagesFragment.setArguments( episodeGuestsArgs );
            tx.add( R.id.episode_guest_images, mEpisodeGuestImagesFragment, TAG_EPISODE_GUEST_IMAGES_FRAGMENT );

        }

        mEpisodeDetailsFragment = (EpisodeDetailsFragment) fm.findFragmentByTag( TAG_EPISODE_DETAILS_FRAGMENT );
        if( null == mEpisodeDetailsFragment ) {
            Log.v( TAG, "updateView : creating new episode details fragment" );

            mEpisodeDetailsFragment = new EpisodeDetailsFragment();
            Bundle episodeDetailsArgs = new Bundle();
            episodeDetailsArgs.putLong( EPISODE_KEY, mEpisodeId );
            episodeDetailsArgs.putInt( EPISODE_NUMBER_KEY, mEpisodeNumber );
            episodeDetailsArgs.putString( SHOW_PREFIX_KEY, mShowPrefix );
            mEpisodeDetailsFragment.setArguments( episodeDetailsArgs );
            tx.add(R.id.episode_details, mEpisodeDetailsFragment, TAG_EPISODE_DETAILS_FRAGMENT);

        }

        mEpisodeImagesFragment = (EpisodeImagesFragment) fm.findFragmentByTag( TAG_EPISODE_IMAGES_FRAGMENT );
        if( null == mEpisodeImagesFragment ) {
            Log.v( TAG, "updateView : creating new episode images fragment" );

            mEpisodeImagesFragment = new EpisodeImagesFragment();
            Bundle episodeImagesArgs = new Bundle();
            episodeImagesArgs.putLong( EPISODE_KEY, mEpisodeId );
            mEpisodeImagesFragment.setArguments( episodeImagesArgs );
            tx.add( R.id.episode_images, mEpisodeImagesFragment, TAG_EPISODE_IMAGES_FRAGMENT );

        }

        if( !tx.isEmpty() ) {
            Log.v( TAG, "updateView : tx contains items that need to be committed" );

            tx.commit();
        }

        Log.v( TAG, "updateView : exit" );
    }

}
