package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmfrey on 7/25/14.
 */
public class Episode {

    public enum Type {
        AUDIO( 0 ),
        VIDOE( 1 );

        private int value;

        Type( int value ) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        private static Map<Integer, Type> valueMap;

        public static Type fromValue( int value ) {

            if( null == valueMap ) {

                valueMap = new HashMap<Integer, Type>();

                for( Type type: values() ) {
                    valueMap.put(type.value, type);
                }

            }

            return valueMap.get( value );
        }

    }

    @SerializedName( "ShowId" )
    private int showId;

    @SerializedName( "Number" )
    private int number;

    @SerializedName( "PostedDate" )
    public String postedDate;

    @SerializedName( "Title" )
    private String title;

    @SerializedName( "VideoFileUrl" )
    private String videoFileUrl;

    @SerializedName( "VideoThumbnailUrl" )
    private String videoThumbnailUrl;

    @SerializedName( "PreviewUrl" )
    private String previewUrl;

    @SerializedName( "FileUrl" )
    private String fileUrl;

    @SerializedName( "Length" )
    private int length;

    @SerializedName( "FileSize" )
    private int fileSize;

    @SerializedName( "Type" )
    private int type;

    @SerializedName( "Public" )
    private boolean notVip;

    @SerializedName( "Timestamp" )
    private long timestamp;

    @SerializedName( "ShowNameId" )
    private int showNameId;

    private Guest[] guests;

    public Episode() { }

    public Episode( int showId, int number, String postedDate, String title, String videoFileUrl, String videoThumbnailUrl, String previewUrl, String fileUrl, int length, int fileSize, int type, boolean notVip, long timestamp, int showNameId ) {

        this.showId = showId;
        this.number = number;
        this.postedDate = postedDate;
        this.title = title;
        this.videoFileUrl = videoFileUrl;
        this.videoThumbnailUrl = videoThumbnailUrl;
        this.previewUrl = previewUrl;
        this.fileUrl = fileUrl;
        this.length = length;
        this.fileSize = fileSize;
        this.type = type;
        this.notVip = notVip;
        this.timestamp = timestamp;
        this.showNameId = showNameId;

    }

    public int getShowId() {
        return showId;
    }

    public void setShowId( int showId ) {
        this.showId = showId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber( int number ) {
        this.number = number;
    }

    public String getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getVideoFileUrl() {
        return videoFileUrl;
    }

    public void setVideoFileUrl( String videoFileUrl ) {
        this.videoFileUrl = videoFileUrl;
    }

    public String getVideoThumbnailUrl() {
        return videoThumbnailUrl;
    }

    public void setVideoThumbnailUrl( String videoThumbnailUrl ) {
        this.videoThumbnailUrl = videoThumbnailUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl( String previewUrl ) {
        this.previewUrl = previewUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl( String fileUrl ) {
        this.fileUrl = fileUrl;
    }

    public int getLength() {
        return length;
    }

    public void setLength( int length ) {
        this.length = length;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize( int fileSize ) {
        this.fileSize = fileSize;
    }

    public int getType() {
        return type;
    }

//    public void setType( Type type ) {
//        this.type = type;
//    }

    public void setType( int type ) {
        this.type = type;
    }

    public boolean isNotVip() {
        return notVip;
    }

    public void setNotVip( boolean notVip ) {
        this.notVip = notVip;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp( long timestamp ) {
        this.timestamp = timestamp;
    }

    public int getShowNameId() {
        return showNameId;
    }

    public void setShowNameId( int showNameId ) {
        this.showNameId = showNameId;
    }

    public Guest[] getGuests() {
        return guests;
    }

    public void setGuests( Guest[] guests ) {
        this.guests = guests;
    }

    @Override
    public String toString() {
        return "Episode{" +
                "showId=" + showId +
                ", number=" + number +
                ", postedDate='" + postedDate + '\'' +
                ", title='" + title + '\'' +
                ", videoFileUrl='" + videoFileUrl + '\'' +
                ", videoThumbnailUrl='" + videoThumbnailUrl + '\'' +
                ", previewUrl='" + previewUrl + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", length=" + length +
                ", fileSize=" + fileSize +
                ", type=" + type +
                ", notVip=" + notVip +
                ", timestamp=" + timestamp +
                ", showNameId=" + showNameId +
                ", guests=" + Arrays.toString(guests) +
                '}';
    }

}
