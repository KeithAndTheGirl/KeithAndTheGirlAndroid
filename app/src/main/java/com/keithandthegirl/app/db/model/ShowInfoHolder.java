package com.keithandthegirl.app.db.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.keithandthegirl.app.ui.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by dmfrey on 7/25/14.
 */
public class ShowInfoHolder {

    private int showNameId;
    private String name;
    private String prefix;
    private boolean vip;
    private int sortOrder;
    private String description;
    private String coverImageUrl;
    private String coverImageUrlSquared;
    private String coverImageUrl200;
    private String coverImageUrl100;
    private String forumUrl;
    private String previewUrl;
    private int episodeCount;
    private int episodeNumberMax;
    private int episodeCountNew;

    public ShowInfoHolder() { }

    public ShowInfoHolder(int showNameId, String name, String prefix, boolean vip, int sortOrder, String description, String coverImageUrl, String coverImageUrlSquared, String coverImageUrl200, String coverImageUrl100, String forumUrl, String previewUrl, int episodeCount, int episodeNumberMax, int episodeCountNew ) {

        this.showNameId = showNameId;
        this.name = name;
        this.prefix = prefix;
        this.vip = vip;
        this.sortOrder = sortOrder;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.coverImageUrlSquared = coverImageUrlSquared;
        this.coverImageUrl200 = coverImageUrl200;
        this.coverImageUrl100 = coverImageUrl100;
        this.forumUrl = forumUrl;
        this.previewUrl = previewUrl;
        this.episodeCount = episodeCount;
        this.episodeNumberMax = episodeNumberMax;
        this.episodeCountNew = episodeCountNew;

    }

    public int getShowNameId() {
        return showNameId;
    }

    public void setShowNameId( int showNameId ) {
        this.showNameId = showNameId;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip( boolean vip ) {
        this.vip = vip;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder( int sortOrder ) {
        this.sortOrder = sortOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl( String coverImageUrl ) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getCoverImageUrlSquared() {
        return coverImageUrlSquared;
    }

    public void setCoverImageUrlSquared( String coverImageUrlSquared ) {
        this.coverImageUrlSquared = coverImageUrlSquared;
    }

    public String getCoverImageUrl200() {
        return coverImageUrl200;
    }

    public void setCoverImageUrl200( String coverImageUrl200 ) {
        this.coverImageUrl200 = coverImageUrl200;
    }

    public String getCoverImageUrl100() {
        return coverImageUrl100;
    }

    public void setCoverImageUrl100( String coverImageUrl100 ) {
        this.coverImageUrl100 = coverImageUrl100;
    }

    public String getForumUrl() {
        return forumUrl;
    }

    public void setForumUrl( String forumUrl ) {
        this.forumUrl = forumUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl( String previewUrl ) {
        this.previewUrl = previewUrl;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount( int episodeCount ) {
        this.episodeCount = episodeCount;
    }

    public int getEpisodeNumberMax() {
        return episodeNumberMax;
    }

    public void setEpisodeNumberMax( int episodeNumberMax ) {
        this.episodeNumberMax = episodeNumberMax;
    }

    public int getEpisodeCountNew() {
        return episodeCountNew;
    }

    public void setEpisodeCountNew(int episodeCountNew) {
        this.episodeCountNew = episodeCountNew;
    }

    @Override
    public String toString() {
        return "Show{" +
                "showNameId=" + showNameId +
                ", name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", vip=" + vip +
                ", sortOrder=" + sortOrder +
                ", description='" + description + '\'' +
                ", coverImageUrl='" + coverImageUrl + '\'' +
                ", coverImageUrlSquared='" + coverImageUrlSquared + '\'' +
                ", coverImageUrl200='" + coverImageUrl200 + '\'' +
                ", coverImageUrl100='" + coverImageUrl100 + '\'' +
                ", forumUrl='" + forumUrl + '\'' +
                ", previewUrl='" + previewUrl + '\'' +
                ", episodeCount=" + episodeCount +
                ", episodeNumberMax=" + episodeNumberMax +
                ", episodeCountNew=" + episodeCountNew +
                '}';
    }

    public static ShowInfoHolder loadShow( Context context, long showId ) {

        ShowInfoHolder showHolder = new ShowInfoHolder();

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query( ContentUris.withAppendedId( ShowConstants.CONTENT_URI, showId ), null, null, null, null );
        if (cursor.moveToNext()) {

            showHolder.setShowNameId( cursor.getInt( cursor.getColumnIndex( ShowConstants._ID ) ) );
            showHolder.setName( cursor.getString( cursor.getColumnIndex( ShowConstants.FIELD_NAME ) ) );
            showHolder.setPrefix(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_PREFIX)));
            showHolder.setVip(cursor.getInt(cursor.getColumnIndex(ShowConstants.FIELD_VIP)) == 0 ? false : true);
            showHolder.setSortOrder(cursor.getInt(cursor.getColumnIndex(ShowConstants.FIELD_SORTORDER)));
            showHolder.setDescription(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_DESCRIPTION)));
            showHolder.setCoverImageUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_COVERIMAGEURL)));
            showHolder.setCoverImageUrlSquared(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_COVERIMAGEURL_SQUARED)));
            showHolder.setCoverImageUrl100(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_COVERIMAGEURL_100)));
            showHolder.setCoverImageUrl200(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_COVERIMAGEURL_200)));
            showHolder.setForumUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_FORUMURL)));
            showHolder.setPreviewUrl(cursor.getString(cursor.getColumnIndex(ShowConstants.FIELD_PREVIEWURL)));
            showHolder.setEpisodeCount(cursor.getInt(cursor.getColumnIndex(ShowConstants.FIELD_EPISODE_COUNT)));
            showHolder.setEpisodeNumberMax(cursor.getInt(cursor.getColumnIndex(ShowConstants.FIELD_EPISODE_COUNT_MAX)));
            showHolder.setEpisodeCountNew(cursor.getInt(cursor.getColumnIndex(ShowConstants.FIELD_EPISODE_COUNT_NEW)));

        }
        cursor.close();

        return showHolder;
    }

}
