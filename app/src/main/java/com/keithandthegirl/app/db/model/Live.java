package com.keithandthegirl.app.db.model;

/**
 * Created by dmfrey on 7/25/14.
 */
public class Live {

    private boolean broadcasting;

    public Live() { }

    public Live( boolean broadcasting ) {
        this.broadcasting = broadcasting;
    }

    public boolean isBroadcasting() {
        return broadcasting;
    }

    public void setBroadcasting( boolean broadcasting ) {
        this.broadcasting = broadcasting;
    }

    @Override
    public String toString() {
        return "Live{" +
                "broadcasting=" + broadcasting +
                '}';
    }

}
