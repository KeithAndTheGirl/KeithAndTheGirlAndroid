package com.keithandthegirl.app.account;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmfrey on 5/28/14.
 */
public class KatgServerAuthenticate implements ServerAuthenticate {

    private static final String TAG = KatgServerAuthenticate.class.getSimpleName();

    @Override
    public String userSignIn( String user, String password, String authType ) throws Exception {
        Log.i( TAG, "userSignIn : enter" );

        String authtoken = null;

        HttpURLConnection conn = null;
        try {

            String urlParameters = "email=" + user + "&password=" + password;

            URL url = new URL( "https://www.keithandthegirl.com/api/v2/vip/authenticateuser/" );
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout( 10000 /* milliseconds */ );
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setDoOutput( true );
            conn.setDoInput( true );
            conn.setInstanceFollowRedirects( false );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            conn.setRequestProperty( "charset", "utf-8" );
            conn.setRequestProperty( "Content-Length", "" + Integer.toString( urlParameters.getBytes().length ) );
            conn.setUseCaches( false );

            DataOutputStream wr = new DataOutputStream( conn.getOutputStream() );
            wr.writeBytes( urlParameters );
            wr.flush();
            wr.close();

            if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                Log.v( TAG, "downloadUrl : HTTP OK" );

                InputStream stream = conn.getInputStream();

                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader( new InputStreamReader( stream, "UTF-8" ), 8 );
                StringBuilder sb = new StringBuilder();

                String line = null;
                while( ( line = reader.readLine() ) != null ) {
                    sb.append( line + "\n" );
                }
                reader.close();
                stream.close();

                JSONObject json = new JSONObject( sb.toString() );
                Log.i( TAG, "userSignIn : json=" + json.toString() );

                boolean error = json.getBoolean( "Error" );
                if( !error ) {

                    String uid = json.getString( "KatgVip_uid" );
                    String key = json.getString( "KatgVip_key" );
                    authtoken = uid + "|" + key;

                } else {

                    String message = json.getString( "message" );
                    throw new Exception( message );

                }

            }

        } catch( JSONException e ) {
            Log.e( TAG, "userSignIn : json exception", e );
            throw new Exception( "json exception", e );

        } catch( IOException e ) {
            Log.e( TAG, "userSignIn : io error", e );
            throw new Exception( "io exception", e );

        } finally {

            if( null != conn ) {

                conn.disconnect();

            }

        }

        Log.i( TAG, "userSignIn : exit" );
        return authtoken;
    }

}
