package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmfrey on 7/25/14.
 */
public class Show {

    @SerializedName( "ShowNameId" )
    private int showNameId;

    @SerializedName( "Name" )
    private String name;

    @SerializedName( "Prefix" )
    private String prefix;

    @SerializedName( "VIP" )
    private boolean vip;

    @SerializedName( "SortOrder" )
    private String sortOrder;

    @SerializedName( "Description" )
    private String description;

    @SerializedName( "CoverImageUrl" )
    private String coverImageUrl;

    @SerializedName( "CoverImageUrl-Squared" )
    private String coverImageUrlSquared;

    @SerializedName( "CoverImageUrl-200" )
    private String coverImageUrl200;

    @SerializedName( "CoverImageUrl-100" )
    private String coverImageUrl100;

    @SerializedName( "ForumUrl" )
    private String forumUrl;

    @SerializedName( "PreviewUrl" )
    private String previewUrl;

    @SerializedName( "EpisodeCount" )
    private int episodeCount;

    @SerializedName( "EpisodeNumberMax" )
    private int episodeNumberMax;

    public Show() { }

    public Show( int showNameId, String name, String prefix, boolean vip, String sortOrder, String description, String coverImageUrl, String coverImageUrlSquared, String coverImageUrl200, String coverImageUrl100, String forumUrl, String previewUrl, int episodeCount, int episodeNumberMax ) {

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

    public String getSortOrder() {
        return sortOrder;
    }

    public int getSortOrderAsInt() {

        if( null == sortOrder || "".equals( sortOrder ) ) {
            return 1;
        }

        return Integer.parseInt( sortOrder );
    }

    public void setSortOrder( String sortOrder ) {
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
                '}';
    }
}
