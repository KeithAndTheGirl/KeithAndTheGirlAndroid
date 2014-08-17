package com.keithandthegirl.app.ui.episodesimpler;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.ui.AbstractBaseActivity;
import com.keithandthegirl.app.ui.player.SimplePlayerActivity;

import java.util.Observable;

public class EpisodeActivity extends AbstractBaseActivity implements EpisodeFragment.EpisodeEventListener {
    public static final String EPISODE_KEY = "EPISODE_KEY";
    private static final String TAG = EpisodeActivity.class.getSimpleName();
    private long mEpisodeId;
    private Button mPlayButton;
    private String mEpisodeFileUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodesimpler);

        Bundle extras = getIntent().getExtras();
        if (extras.containsKey(EPISODE_KEY)) {
            mEpisodeId = extras.getLong(EPISODE_KEY);
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, EpisodeFragment.newInstance(mEpisodeId))
                    .commit();
        }

        mPlayButton = (Button) findViewById(R.id.play);
        mPlayButton.setEnabled(false);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent mpdIntent = new Intent(EpisodeActivity.this, SimplePlayerActivity.class)
                        .setData(Uri.parse(mEpisodeFileUrl));
//                        .putExtra( DemoUtil.CONTENT_ID_EXTRA, sample.contentId )
//                        .putExtra( DemoUtil.CONTENT_TYPE_EXTRA, sample.type );
                startActivity(mpdIntent);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.episode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEpisodeLoaded(final String episodeFileUrl) {
        mEpisodeFileUrl = episodeFileUrl;
        mPlayButton.setEnabled(true);
        // TODO Enable UI better now that we have episodeId
    }
}
