package com.keithandthegirl.app.db.model;

import java.util.Arrays;

/**
 * Created by dmfrey on 7/26/14.
 */
public class Events {

    private Event[] events;

    public Events() { }

    public Events( Event[] events ) {

        this.events = events;

    }

    public Event[] getEvents() {
        return events;
    }

    public void setEvents( Event[] events ) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "Events{" +
                "events=" + Arrays.toString(events) +
                '}';
    }

}
