package com.keithandthegirl.app.sync;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.Youtube;
import com.keithandthegirl.app.db.model.YoutubeConstants;
import com.keithandthegirl.app.db.model.YoutubeEntry;
import com.keithandthegirl.app.db.model.YoutubeLink;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by dmfrey on 1/19/15.
 */
public class YoutubeLoaderAsyncTask extends AsyncTask<Void, Void, Youtube> {

    private static final String TAG = YoutubeLoaderAsyncTask.class.getSimpleName();

    Context mContext;
    YoutubeService mYoutubeService;

    public YoutubeLoaderAsyncTask( Context context ) {

        mContext = context;

        initializeClient();

    }

    @Override
    protected Youtube doInBackground( Void... params ) {
        Log.v( TAG, "doInBackground : enter" );

        try {
            Youtube youtube = mYoutubeService.listKatgYoutubeFeed();

            Log.v( TAG, "doInBackground : exit" );
            return youtube;
        } catch( RetrofitError e ) {

            Log.v( TAG, "doInBackground : error", e );
            return null;
        }

    }

    @Override
    protected void onPostExecute( Youtube youtube ) {
        Log.v( TAG, "onPostExecute : enter" );

        if( null != youtube ) {

            if( null != youtube.getFeed() ) {

                if( null != youtube.getFeed().getEntries() && youtube.getFeed().getEntries().length > 0 ) {

                    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

                    DateTime now = new DateTime( DateTimeZone.UTC );
                    String[] projection = new String[] { YoutubeConstants._ID };

                    for( YoutubeEntry entry : youtube.getFeed().getEntries() ) {

                        String content = "", thumbnail = "";
                        try {
                            content = entry.getContent().getContent();
                        } catch( Exception e ) {
                            //Log.v( TAG, "processYoutubeEpisodes : content is not valid" );
                        }

                        if( !"".equals( content ) ) {
                            String img = content.substring( content.indexOf( "<img" ) );
                            img = img.substring( 0, img.indexOf( "</a>" ) - 2 );

                            thumbnail = img.substring( img.indexOf( "src=" ) + 5 );
                        }

                        String etag = "";
                        try {
                            etag = entry.getEtag();
                        } catch( Exception e ) {
                            //Log.v( TAG, "processYoutubeEpisodes : etag is not valid" );
                        }

                        String youtubeId = "";
                        try {
                            youtubeId = entry.getId().getValue();
                        } catch( Exception e ) {
                            //Log.v( TAG, "processYoutubeEpisodes : id is not valid" );
                        } finally {
                            if( !"".equals( youtubeId ) ) {
                                youtubeId = youtubeId.substring( youtubeId.lastIndexOf( ':' ) + 1 );
                            }
                        }

                        DateTime published = new DateTime();
                        try {
                            published = new DateTime( entry.getPublished() );
                        } catch( Exception e ) {
                            //Log.v( TAG, "processYoutubeEpisodes : published is not valid" );
                        } finally {
                            published = published.withZone( DateTimeZone.UTC );
                        }

                        DateTime updated = new DateTime();
                        try {
                            updated = new DateTime( entry.getUpdated() );
                        } catch( Exception e ) {
                            //Log.v( TAG, "processYoutubeEpisodes : updated is not valid" );
                        } finally {
                            updated = updated.withZone( DateTimeZone.UTC );
                        }

                        String title = "";
                        try {
                            title = entry.getTitle().getValue();
                        } catch( Exception e ) {
                            //Log.v( TAG, "processYoutubeEpisodes : title is not valid" );
                        }

                        String link = "";
                        if( null != entry.getLinks() && entry.getLinks().length > 0 ) {

                            for( YoutubeLink youtubeLink : entry.getLinks() ) {

                                if( "alternate".equals( youtubeLink.getRel() ) ) {
                                    link = youtubeLink.getHref();
                                }

                            }

                        }

                        ContentValues values = new ContentValues();
                        values.put( YoutubeConstants.FIELD_YOUTUBE_ID, youtubeId );
                        values.put( YoutubeConstants.FIELD_YOUTUBE_ETAG, etag );
                        values.put( YoutubeConstants.FIELD_YOUTUBE_TITLE, title );
                        values.put( YoutubeConstants.FIELD_YOUTUBE_LINK, link );
                        values.put( YoutubeConstants.FIELD_YOUTUBE_THUMBNAIL, thumbnail );
                        values.put( YoutubeConstants.FIELD_YOUTUBE_PUBLISHED, published.getMillis() );
                        values.put( YoutubeConstants.FIELD_YOUTUBE_UPDATED, updated.getMillis() );
                        values.put( YoutubeConstants.FIELD_LAST_MODIFIED_DATE, now.getMillis() );

                        Cursor cursor = mContext.getContentResolver().query( YoutubeConstants.CONTENT_URI, projection, YoutubeConstants.FIELD_YOUTUBE_ID + " = ?", new String[] { youtubeId }, null );
                        if( cursor.moveToFirst() ) {
                            //Log.v( TAG, "processYoutubeEpisodes : updating existing entry" );

                            Long id = cursor.getLong( cursor.getColumnIndexOrThrow( YoutubeConstants._ID ) );
                            ops.add(
                                    ContentProviderOperation
                                            .newUpdate(ContentUris.withAppendedId(YoutubeConstants.CONTENT_URI, id))
                                            .withValues(values)
                                            .build()
                            );

                        } else {
                            //Log.v( TAG, "processYoutubeEpisodes : adding new entry" );

                            ops.add(
                                    ContentProviderOperation
                                            .newInsert(YoutubeConstants.CONTENT_URI)
                                            .withValues(values)
                                            .withYieldAllowed(true)
                                            .build()
                            );

                        }
                        cursor.close();

                    }

                    try {

                        mContext.getContentResolver().applyBatch( KatgProvider.AUTHORITY, ops );

                    } catch( Exception e ) {

                        // Display warning
                        CharSequence txt = mContext.getString( R.string.processYoutubeVideosFailure );
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText( mContext, txt, duration );
                        toast.show();

                        // Log exception
                        Log.e( TAG, "onPostExecute : error processing youtube", e );
                    }

                }

            }

        }

        Log.v( TAG, "onPostExecute : exit" );
    }

    private void initializeClient() {

        OkHttpClient client = new OkHttpClient();

        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        File cacheDirectory = new File( mContext.getCacheDir().getAbsolutePath(), "HttpCache" );

        Cache cache = new Cache( cacheDirectory, cacheSize );
        client.setCache( cache );

        Gson youtubeGson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        RestAdapter youtubeRestAdapter = new RestAdapter.Builder()
                .setEndpoint( YoutubeService.YOUTUBE_KATG_URL )
                .setClient( new OkClient( client ) )
                .setConverter( new GsonConverter( youtubeGson ) )
//                .setLogLevel( RestAdapter.//LogLevel.FULL )
                .build();

        mYoutubeService = youtubeRestAdapter.create( YoutubeService.class );

    }

}
