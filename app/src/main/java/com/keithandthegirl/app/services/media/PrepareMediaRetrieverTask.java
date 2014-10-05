package com.keithandthegirl.app.services.media;

import android.os.AsyncTask;

/**
 * Asynchronous task that prepares a MusicRetriever. This asynchronous task essentially calls
 * {@link MediaRetriever#prepare()} on a {@link MediaRetriever}, which may take some time to
 * run. Upon finishing, it notifies the indicated {@MediaRetrieverPreparedListener}.
 */
public class PrepareMediaRetrieverTask extends AsyncTask<Void, Void, Void> {
    MediaRetriever mRetriever;
    MediaRetrieverPreparedListener mListener;

    public PrepareMediaRetrieverTask(MediaRetriever retriever,
                                     MediaRetrieverPreparedListener listener) {
        mRetriever = retriever;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        mRetriever.prepare();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mListener.onMediaRetrieverPrepared();
    }

    public interface MediaRetrieverPreparedListener {
        public void onMediaRetrieverPrepared();
    }

}
