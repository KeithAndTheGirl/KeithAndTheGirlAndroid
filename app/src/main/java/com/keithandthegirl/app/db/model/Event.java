package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by dmfrey on 7/25/14.
 */
public class Event {

    private DateFormat df = new SimpleDateFormat( "MM/dd/yyyy hh:mm" );

    @SerializedName( "eventid" )
    private String eventId;

    private String title;

    private String location;

    @SerializedName( "startdate" )
    private Date startDate;

    @SerializedName( "enddate" )
    private Date endDate;

    private String details;

    public Event() { }

    public Event( String eventId, String title, String location, Date startDate, Date endDate, String details ) {

        this.eventId = eventId;
        this.title = title;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.details = details;

    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId( String eventId ) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation( String location ) {
        this.location = location;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate( Date startDate ) {
        this.startDate = startDate;
    }

    public void setStartDate( String startDate ) {

        try {

            this.startDate = df.parse( startDate );

        } catch( ParseException e ) { }

    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate( Date endDate ) {
        this.endDate = endDate;
    }

    public void setEndDate( String endDate ) {

        try {

            this.endDate = df.parse( endDate );

        } catch( ParseException e ) { }

    }

    public String getDetails() {
        return details;
    }

    public void setDetails( String details ) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", details='" + details + '\'' +
                '}';
    }

}
