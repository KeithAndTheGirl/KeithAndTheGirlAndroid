package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by dmfrey on 7/28/14.
 */
public class YoutubeFeed {

    @SerializedName( "author" )
    private YoutubeAuthor[] authors;

    @SerializedName( "category" )
    private YoutubeCategory[] categories;

    @SerializedName( "entry" )
    private YoutubeEntry[] entries;

    public YoutubeFeed() { }

    public YoutubeAuthor[] getAuthors() {
        return authors;
    }

    public void setAuthors( YoutubeAuthor[] authors ) {

        this.authors = authors;

    }

    public YoutubeCategory[] getCategories() {
        return categories;
    }

    public void setCategories( YoutubeCategory[] categories ) {

        this.categories = categories;

    }

    public YoutubeEntry[] getEntries() {
        return entries;
    }

    public void setEntries( YoutubeEntry[] entries ) {

        this.entries = entries;

    }

    @Override
    public String toString() {
        return "YoutubeFeed{" +
                "authors=" + Arrays.toString(authors) +
                ", categories=" + Arrays.toString(categories) +
                ", entries=" + Arrays.toString(entries) +
                '}';
    }

}
