package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmfrey on 7/28/14.
 */
public class YoutubeValue {

    @SerializedName( "$t" )
    private String value;

    public YoutubeValue() { }

    public YoutubeValue( String value ) {

        this.value = value;

    }

    public String getValue() {
        return value;
    }

    public void setValue( String value ) {

        this.value = value;

    }

    @Override
    public String toString() {
        return "YoutubeValue{" +
                "value='" + value + '\'' +
                '}';
    }

}
