package com.keithandthegirl.app.sync;

import com.keithandthegirl.app.db.model.Detail;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.Events;
import com.keithandthegirl.app.db.model.Live;
import com.keithandthegirl.app.db.model.Show;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by dmfrey on 7/25/14.
 */
public interface KatgService {

    public static final String KATG_URL = "https://www.keithandthegirl.com/api/v2/";

    @GET( "/events/" )
    Events events();

    @GET( "/feed/live" )
    Live broadcasting();

    @GET( "/shows/details/" )
    Detail showDetails( @Query( "showId" ) int showId, @Query( "explicit" ) int explicit );

    @GET( "/shows/recent/" )
    List<Episode> recentEpisodes();

    @GET( "/shows/list/" )
    List<Episode> listEpisodes( @Query( "shownameid" ) int showNameId, @Query( "showid" ) int showId, @Query( "number" ) int number, @Query( "limit" ) int limit );

    @GET( "/shows/series-overview" )
    List<Show> seriesOverview();

}
