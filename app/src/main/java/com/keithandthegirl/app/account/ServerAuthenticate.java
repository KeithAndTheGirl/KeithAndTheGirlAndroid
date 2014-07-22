package com.keithandthegirl.app.account;

public interface ServerAuthenticate {

    public String userSignIn( final String user, final String pass, String authType ) throws Exception;

}