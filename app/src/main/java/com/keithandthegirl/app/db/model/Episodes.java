package com.keithandthegirl.app.db.model;

import java.util.List;

/**
 * Created by dmfrey on 3/25/15.
 */
public class Episodes {

    private List<Episode> episodes;

    public Episodes() { }

    public List<Episode> getEpisodes() {

        return episodes;
    }

    public void setEpisodes( List<Episode> episodes ) {

        this.episodes = episodes;

    }

}
