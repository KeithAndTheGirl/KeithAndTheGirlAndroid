package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmfrey on 7/28/14.
 */
public class Youtube {

    @SerializedName( "encoding" )
    private String encoding;

    @SerializedName( "feed" )
    private YoutubeFeed feed;

    public Youtube() { }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding( String encoding ) {

        this.encoding = encoding;

    }

    public YoutubeFeed getFeed() {
        return feed;
    }

    public void setFeed( YoutubeFeed feed ) {

        this.feed = feed;

    }

    @Override
    public String toString() {
        return "Youtube{" +
                "encoding='" + encoding + '\'' +
                ", feed=" + feed +
                '}';
    }

}
