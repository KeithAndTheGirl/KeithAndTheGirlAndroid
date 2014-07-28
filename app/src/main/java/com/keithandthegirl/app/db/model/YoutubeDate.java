package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by dmfrey on 7/28/14.
 */
public class YoutubeDate {

    @SerializedName( "$t" )
    private Date value;

    public YoutubeDate() { }

    public YoutubeDate( Date value ) {

        this.value = value;

    }

    public Date getValue() {
        return value;
    }

    public void setValue( Date value ) {

        this.value = value;

    }

    @Override
    public String toString() {
        return "YoutubeDate{" +
                "value=" + value +
                '}';
    }

}
