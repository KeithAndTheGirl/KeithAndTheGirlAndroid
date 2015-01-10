package com.keithandthegirl.app.sync;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Jeff on 12/23/2014.
 */
public final class EmptyRetrofitCallback {

    private static final Callback<Object> EMPTY = new Callback<Object>() {
        @Override
        public void success(final Object appHeartbeatResponse, final Response response) {}

        @Override
        public void failure(final RetrofitError error) { }
    };

    @SuppressWarnings("unchecked")
    public static <T> Callback<T> empty() {
        return (Callback<T>) EMPTY;
    }
}
