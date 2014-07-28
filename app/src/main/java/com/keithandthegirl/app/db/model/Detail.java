package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by dmfrey on 7/25/14.
 */
public class Detail {

    private String notes;

    @SerializedName( "forum_url" )
    private String forumUrl;

    @SerializedName( "preview_url" )
    private String previewUrl;

    private Image[] images;

    public Detail() { }

    public Detail( String notes, String forumUrl, String previewUrl ) {
        this.notes = notes;
        this.forumUrl = forumUrl;
        this.previewUrl = previewUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes( String notes ) {
        this.notes = notes;
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

    public Image[] getImages() {
        return images;
    }

    public void setImages( Image[] images ) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "Detail{" +
                "notes='" + notes + '\'' +
                ", forumUrl='" + forumUrl + '\'' +
                ", previewUrl='" + previewUrl + '\'' +
                ", images=" + Arrays.toString(images) +
                '}';
    }

}
