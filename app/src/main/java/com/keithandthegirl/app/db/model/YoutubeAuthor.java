package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmfrey on 7/28/14.
 */
public class YoutubeAuthor {

    @SerializedName( "name" )
    private YoutubeValue name;

    @SerializedName( "uri" )
    private YoutubeValue uri;

    public YoutubeAuthor() { }

    public YoutubeValue getName() {
        return name;
    }

    public void setName( YoutubeValue name ) {

        this.name = name;

    }

    public YoutubeValue getUri() {
        return uri;
    }

    public void setUri( YoutubeValue uri ) {

        this.uri = uri;

    }

    @Override
    public String toString() {
        return "YoutubeAuthor{" +
                "name=" + name +
                ", uri=" + uri +
                '}';
    }

}
