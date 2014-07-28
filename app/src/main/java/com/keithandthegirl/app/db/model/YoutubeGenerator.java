package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmfrey on 7/28/14.
 */
public class YoutubeGenerator {

    @SerializedName( "$t" )
    private String value;

    @SerializedName( "uri" )
    private String uri;

    @SerializedName( "version" )
    private String version;

    public YoutubeGenerator() { }

    public String getValue() {
        return value;
    }

    public void setValue( String value ) {

        this.value = value;

    }

    public String getUri() {
        return uri;
    }

    public void setUri( String uri ) {

        this.uri = uri;

    }

    public String getVersion() {
        return version;
    }

    public void setVersion( String version ) {

        this.version = version;

    }

    @Override
    public String toString() {
        return "YoutubeGenerator{" +
                "value='" + value + '\'' +
                ", uri='" + uri + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

}
