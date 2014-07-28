package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmfrey on 7/25/14.
 */
public class Image {

    @SerializedName( "pictureid" )
    private int pictureId;

    private String title;

    private String description;

    private boolean explicit;

    @SerializedName( "displayorder" )
    private int displayOrder;

    @SerializedName( "media_url" )
    private String mediaUrl;

    public Image() { }

    public Image( int pictureId, String title, String description, boolean explicit, int displayOrder, String mediaUrl ) {

        this.pictureId = pictureId;
        this.title = title;
        this.description = description;
        this.explicit = explicit;
        this.displayOrder = displayOrder;
        this.mediaUrl = mediaUrl;

    }

    public int getPictureId() {
        return pictureId;
    }

    public void setPictureId( int pictureId ) {
        this.pictureId = pictureId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit( boolean explicit ) {
        this.explicit = explicit;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder( int displayOrder ) {
        this.displayOrder = displayOrder;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl( String mediaUrl ) {
        this.mediaUrl = mediaUrl;
    }

    @Override
    public String toString() {
        return "Image{" +
                "pictureId=" + pictureId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", explicit=" + explicit +
                ", displayOrder=" + displayOrder +
                ", mediaUrl='" + mediaUrl + '\'' +
                '}';
    }

}
