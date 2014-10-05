package com.keithandthegirl.app.services.media;

import android.os.AsyncTask;

/**
 * Asynchronous task that prepares a MusicRetriever. This asynchronous task essentially calls
 * {@link com.keithandthegirl.app.services.media.MediaRetriever#prepare()} on a {@link com.keithandthegirl.app.services.media.MediaRetriever}, which may take some time to
 * run. Upon finishing, it notifies the indicated {@MediaRetrieverPreparedListener}.
 */
public class PrepareEpisodeRetrieverTask extends AsyncTask<Void, Void, Void> {
    EpisodeRetriever mRetriever;
    EpisodeRetrieverPreparedListener mListener;

    public PrepareEpisodeRetrieverTask(EpisodeRetriever retriever,
                                       EpisodeRetrieverPreparedListener listener) {
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
        mListener.onEpisodeRetrieverPrepared();
    }

    public interface EpisodeRetrieverPreparedListener {
        public void onEpisodeRetrieverPrepared();
    }

}
