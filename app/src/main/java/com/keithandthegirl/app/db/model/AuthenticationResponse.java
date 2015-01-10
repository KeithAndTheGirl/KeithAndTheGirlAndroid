package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jeff on 12/16/2014.
 */
public class AuthenticationResponse {

    @SerializedName( "KatgVip_uid" )
    private String mVipUid;
    @SerializedName( "KatgVip_key" )
    private String mVipKey;
    @SerializedName( "Error" )
    private Boolean mError;
    @SerializedName( "Message" )
    private String mMessage;

    public String getVipUid() {
        return mVipUid;
    }

    public void setVipUid(final String vipUid) {
        mVipUid = vipUid;
    }

    public String getVipKey() {
        return mVipKey;
    }

    public void setVipKey(final String vipKey) {
        mVipKey = vipKey;
    }

    public Boolean getError() {
        return mError;
    }

    public void setError(final Boolean error) {
        mError = error;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(final String message) {
        mMessage = message;
    }

    public AuthenticationResponse() {}
}
