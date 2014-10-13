package com.keithandthegirl.app.db.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jeff on 8/15/2014.
 * Copyright JeffInMadison.com 2014
 */
public class EpisodeInfoHolder {
    private int mEpisodeNumber;
    private String mEpisodeTitle;
    private String mEpisodePreviewUrl;
    private String mEpisodeFileUrl;
    private String mEpisodeFilename;
    private int mEpisodeLength;
    private int mEpisodeFileSize;
    private int mEpisodeType;
    private boolean mEpisodePublic;
    private String mEpisodePosted;
    private long mEpisodeDownloadId;
    private boolean mEpisodeDownloaded;
    private int mEpisodePlayed;
    private int mEpisodeLastPlayed;
    private int mShowNameId;
    private String mEpisodeDetailNotes;
    private String mEpisodeDetailForumUrl;
    private String mShowName;
    private String mShowPrefix;
    private boolean mShowVip;
    private String mShowCoverImageUrl;
    private String mShowForumUrl;
    private String mGuestNames;
    private List<String> mGuestImages;
    private List<String> mEpisodeImages;

    public int getEpisodeNumber() {
        return mEpisodeNumber;
    }
    public void setEpisodeNumber(final int episodeNumber) {
        mEpisodeNumber = episodeNumber;
    }

    public String getEpisodeTitle() {
        return mEpisodeTitle;
    }
    public void setEpisodeTitle(final String episodeTitle) {
        mEpisodeTitle = episodeTitle;
    }

    public String getEpisodePreviewUrl() {
        return mEpisodePreviewUrl;
    }
    public void setEpisodePreviewUrl(final String episodePreviewUrl) {
        mEpisodePreviewUrl = episodePreviewUrl;
    }

    public String getEpisodeFileUrl() {
        return mEpisodeFileUrl;
    }
    public void setEpisodeFileUrl(final String episodeFileUrl) {
        mEpisodeFileUrl = episodeFileUrl;
    }

    public String getEpisodeFilename() {
        return mEpisodeFilename;
    }
    public void setEpisodeFilename(final String episodeFilename) {
        mEpisodeFilename = episodeFilename;
    }

    public int getEpisodeLength() {
        return mEpisodeLength;
    }
    public void setEpisodeLength(final int episodeLength) {
        mEpisodeLength = episodeLength;
    }

    public int getEpisodeFileSize() {
        return mEpisodeFileSize;
    }
    public void setEpisodeFileSize(final int episodeFileSize) {
        mEpisodeFileSize = episodeFileSize;
    }

    public int getEpisodeType() {
        return mEpisodeType;
    }
    public void setEpisodeType(final int episodeType) {
        mEpisodeType = episodeType;
    }

    public boolean isEpisodePublic() {
        return mEpisodePublic;
    }
    public void setEpisodePublic(final boolean episodePublic) {
        mEpisodePublic = episodePublic;
    }

    public String getEpisodePosted() {
        return mEpisodePosted;
    }
    public void setEpisodePosted(final String episodePosted) {
        mEpisodePosted = episodePosted;
    }

    public long getEpisodeDownloadId() {
        return mEpisodeDownloadId;
    }
    public void setEpisodeDownloadId(final long episodeDownloadId) {
        mEpisodeDownloadId = episodeDownloadId;
    }

    public boolean isEpisodeDownloaded() {
        return mEpisodeDownloaded;
    }
    public void setEpisodeDownloaded(final boolean episodeDownloaded) {
        mEpisodeDownloaded = episodeDownloaded;
    }

    public int getEpisodePlayed() {
        return mEpisodePlayed;
    }
    public void setEpisodePlayed(final int episodePlayed) {
        mEpisodePlayed = episodePlayed;
    }

    public int getEpisodeLastPlayed() {
        return mEpisodeLastPlayed;
    }
    public void setEpisodeLastPlayed(final int episodeLastPlayed) {
        mEpisodeLastPlayed = episodeLastPlayed;
    }

    public int getShowNameId() {
        return mShowNameId;
    }
    public void setShowNameId(final int showNameId) {
        mShowNameId = showNameId;
    }

    public String getEpisodeDetailNotes() {
        return mEpisodeDetailNotes;
    }
    public void setEpisodeDetailNotes(final String episodeDetailNotes) {
        mEpisodeDetailNotes = episodeDetailNotes;
    }

    public String getEpisodeDetailForumUrl() {
        return mEpisodeDetailForumUrl;
    }
    public void setEpisodeDetailForumUrl(final String episodeDetailForumUrl) {
        mEpisodeDetailForumUrl = episodeDetailForumUrl;
    }

    public String getShowName() {
        return mShowName;
    }
    public void setShowName(final String showName) {
        mShowName = showName;
    }

    public String getShowPrefix() {
        return mShowPrefix;
    }
    public void setShowPrefix(final String showPrefix) {
        mShowPrefix = showPrefix;
    }

    public boolean isShowVip() {
        return mShowVip;
    }
    public void setShowVip(final boolean showVip) {
        mShowVip = showVip;
    }

    public String getShowCoverImageUrl() {
        return mShowCoverImageUrl;
    }
    public void setShowCoverImageUrl(final String showCoverImageUrl) {
        mShowCoverImageUrl = showCoverImageUrl;
    }

    public String getShowForumUrl() {
        return mShowForumUrl;
    }
    public void setShowForumUrl(final String showForumUrl) {
        mShowForumUrl = showForumUrl;
    }

    public String getGuestNames() {
        return mGuestNames;
    }
    public void setGuestNames(final String guestNames) {
        mGuestNames = guestNames;
    }

    public List<String> getEpisodeGuestImages() {
        return mGuestImages;
    }
    public void setEpisodeGuestImages(final List<String> guestImages) {
        mGuestImages = guestImages;
    }

    public List<String> getEpisodeImages() {
        return mEpisodeImages;
    }
    public void setEpisodeImages(final List<String> episodeImages) {
        mEpisodeImages = episodeImages;
    }

    public static EpisodeInfoHolder loadEpisode( Context context, long episodeId ) {

        EpisodeInfoHolder episodeHolder = new EpisodeInfoHolder();

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContentUris.withAppendedId(EpisodeConstants.CONTENT_URI, episodeId), null, null, null, null);
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
        String[] selectionArgs = new String[]{String.valueOf(episodeId)};

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

}