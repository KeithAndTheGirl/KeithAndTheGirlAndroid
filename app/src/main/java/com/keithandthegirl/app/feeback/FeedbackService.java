package com.keithandthegirl.app.feeback;

import com.google.gson.annotations.SerializedName;
import com.keithandthegirl.app.db.model.Live;
import com.keithandthegirl.app.sync.KatgService;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by Jeff on 12/5/2014.
 */
public class FeedbackService {
    private static final String TAG = FeedbackService.class.getSimpleName();
    private static FeedbackService mFeedbackService;
    private static final String FEEDBACK_URL = "http://www.attackwork.com";

    public interface FeedbackEndpoints {
        //http://www.attackwork.com/Voxback/Comment-Form-Iframe.aspx?VoxbackId=3&MixerCode=IEOSE&Response-API=yes
        @FormUrlEncoded
        @POST("/Voxback/Comment-Form-Iframe.aspx?VoxbackId=3&MixerCode=IEOSE&Response-API=yes")
        FeedbackResult sendFeedback(@Field("Name") String name,
                                    @Field("Location") String Location,
                                    @Field("Comment") String Comment,
                                    @Field("HiddenMixerCode") String mixerCode,
                                    @Field("HiddenVoxbackId") String voxbackId);

        //http://www.attackwork.com/Voxback/Comment-Form-Iframe.aspx?VoxbackId=3&MixerCode=IEOSE&Response-API=yes
        @FormUrlEncoded
        @POST("/Voxback/Comment-Form-Iframe.aspx?VoxbackId=3&MixerCode=IEOSE&Response-API=yes")
        void sendFeedback(@Field("Name") String name,
                          @Field("Location") String location,
                          @Field("Comment") String comment,
                          @Field("HiddenMixerCode") String mixerCode,
                          @Field("HiddenVoxbackId") String voxbackId,
                          Callback<FeedbackResult> callback);
    }
    public interface KatgEndpoints {
        @GET( "/feed/live" )
        Live getBroadcastingLive();

        @GET( "/feed/live" )
        void getBroadcastingLive(Callback<Live> callback);
    }

    private static FeedbackEndpoints mFeedbackEndpoint;
    private static KatgEndpoints mKatgEndpoint;


    public static FeedbackService getInstance() {
        if (mFeedbackService == null) {
            mFeedbackService = new FeedbackService();
        }

        return mFeedbackService;
    }

    private FeedbackService() {
        OkHttpClient client = new OkHttpClient();

        RestAdapter feedbackRestAdapter = new RestAdapter.Builder()
                .setEndpoint(FEEDBACK_URL)
                .setClient(new OkClient(client))
                .build();

        mFeedbackEndpoint = feedbackRestAdapter.create(FeedbackEndpoints.class);

        RestAdapter katgRestAdapter = new RestAdapter.Builder()
                .setEndpoint(KatgService.KATG_URL)
                .setClient(new OkClient(client))
                .build();

        mKatgEndpoint = katgRestAdapter.create(KatgEndpoints.class);
    }

    public FeedbackResult sendFeedback(String name, String location, String comment) {
        return mFeedbackEndpoint.sendFeedback(name, location, comment, "IEOSE", "3");
    }

    public void sendFeedback(String username, String location, String comment, Callback<FeedbackResult> callback) {
        mFeedbackEndpoint.sendFeedback(username, location, comment, "IEOSE", "3", callback);
    }

    public Live getBroadcastingLive() {
        return mKatgEndpoint.getBroadcastingLive();
    }

    public void getBroadcastingLive(Callback<Live> callback) {
        mKatgEndpoint.getBroadcastingLive(callback);
    }

    public static class FeedbackResult {
        @SerializedName("error")
        private Boolean mError;
        @SerializedName("response")
        private List<String> mResponse;
        @SerializedName("formdata")
        private List<String> mFormData;

        public Boolean getError() {
            return mError;
        }

        public void setError(final Boolean error) {
            mError = error;
        }

        public List<String> getResponse() {
            return mResponse;
        }

        public void setResponse(final List<String> response) {
            mResponse = response;
        }
    }
}
