package com.keithandthegirl.app.sync;

import com.keithandthegirl.app.db.model.Youtube;

import retrofit.http.GET;

/**
 * Created by dmfrey on 7/28/14.
 */
public interface YoutubeService {

    public static final String YOUTUBE_KATG_URL = "http://gdata.youtube.com";

    @GET( "/feeds/base/users/keithandthegirl/uploads?alt=json&v=2&orderby=published&client=ytapi-youtube-profile" )
    Youtube listKatgYoutubeFeed();

}
