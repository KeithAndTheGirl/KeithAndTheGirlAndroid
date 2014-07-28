package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmfrey on 7/28/14.
 */
public class YoutubeCategory {

    @SerializedName( "scheme" )
    private String scheme;

    @SerializedName( "term" )
    private String term;

    public YoutubeCategory() { }

    public String getScheme() {
        return scheme;
    }

    public void setScheme( String scheme ) {

        this.scheme = scheme;

    }

    public String getTerm() {
        return term;
    }

    public void setTerm( String term ) {

        this.term = term;

    }

    @Override
    public String toString() {
        return "YoutubeCategory{" +
                "scheme=" + scheme +
                ", term=" + term +
                '}';
    }

}
