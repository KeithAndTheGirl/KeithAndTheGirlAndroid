package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmfrey on 7/25/14.
 */
public class Guest {

    @SerializedName( "Data" )
    private int data;

    @SerializedName( "ShowGuestId" )
    private int showGuestId;

    @SerializedName( "RealName" )
    private String realName;

    @SerializedName( "Description" )
    private String description;

    @SerializedName( "PictureFilename" )
    private String pictureFilename;

    @SerializedName( "Url1" )
    private String url1;

    @SerializedName( "Url2" )
    private String url2;

    @SerializedName( "PictureUrl" )
    private String pictureUrl;

    @SerializedName( "PictureUrlLarge" )
    private String pictureUrlLarge;

    public Guest() { }

    public Guest( int data, int showGuestId, String realName, String description, String pictureFilename, String url1, String url2, String pictureUrl, String pictureUrlLarge ) {

        this.data = data;
        this.showGuestId = showGuestId;
        this.realName = realName;
        this.description = description;
        this.pictureFilename = pictureFilename;
        this.url1 = url1;
        this.url2 = url2;
        this.pictureUrl = pictureUrl;
        this.pictureUrlLarge = pictureUrlLarge;

    }

    public int getData() {
        return data;
    }

    public void setData( int data ) {
        this.data = data;
    }

    public int getShowGuestId() {
        return showGuestId;
    }

    public void setShowGuestId( int showGuestId ) {
        this.showGuestId = showGuestId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName( String realName ) {
        this.realName = realName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public String getPictureFilename() {
        return pictureFilename;
    }

    public void setPictureFilename( String pictureFilename ) {
        this.pictureFilename = pictureFilename;
    }

    public String getUrl1() {
        return url1;
    }

    public void setUrl1( String url1 ) {
        this.url1 = url1;
    }

    public String getUrl2() {
        return url2;
    }

    public void setUrl2( String url2 ) {
        this.url2 = url2;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl( String pictureUrl ) {
        this.pictureUrl = pictureUrl;
    }

    public String getPictureUrlLarge() {
        return pictureUrlLarge;
    }

    public void setPictureUrlLarge( String pictureUrlLarge ) {
        this.pictureUrlLarge = pictureUrlLarge;
    }

    @Override
    public String toString() {
        return "Guest{" +
                "data=" + data +
                ", showGuestId=" + showGuestId +
                ", realName='" + realName + '\'' +
                ", description='" + description + '\'' +
                ", pictureFilename='" + pictureFilename + '\'' +
                ", url1='" + url1 + '\'' +
                ", url2='" + url2 + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", pictureUrlLarge='" + pictureUrlLarge + '\'' +
                '}';
    }

}
