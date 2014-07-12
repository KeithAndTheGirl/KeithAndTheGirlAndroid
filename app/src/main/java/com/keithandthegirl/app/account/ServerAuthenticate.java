package com.keithandthegirl.app.account;

import java.util.Map;

public interface ServerAuthenticate {

    public String userSignIn( final String user, final String pass, String authType ) throws Exception;

}