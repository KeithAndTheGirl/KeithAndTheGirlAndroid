package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmfrey on 7/28/14.
 */
public class YoutubeContent {

    @SerializedName( "$t" )
    private String content;

    @SerializedName( "type" )
    private String type;

    public YoutubeContent() { }

    public String getContent() {
        return content;
    }

    public void setContent( String content ) {

        this.content = content;

    }

    public String getType() {
        return type;
    }

    public void setType( String type ) {

        this.type = type;

    }

    @Override
    public String toString() {
        return "YoutubeContent{" +
                "content=" + content +
                ", type='" + type + '\'' +
                '}';
    }

}
