package com.chancesnow.party;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;

import kaaes.spotify.webapi.android.models.Track;

public class QueueActivity extends AppCompatActivity
        implements PlayerFragment.OnPlayerInteractionListener {

    private View mQueueActivity;
    private Toolbar mToolbar;

    private View mLoadingView;
    private Button mShuffleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_queue);

        mQueueActivity = findViewById(R.id.queue);

        mToolbar = (Toolbar) findViewById(R.id.playlists_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setTitle(R.string.queue);
        }

        mLoadingView = findViewById(R.id.playlists_loading);
//        if (mLoadingView != null)
//            mLoadingView.setVisibility(View.GONE);

        mShuffleButton = (Button) findViewById(R.id.player_shuffle);
        if (mShuffleButton != null)
            mShuffleButton.setCompoundDrawablesRelative(
                    new IconDrawable(this, MaterialCommunityIcons.mdi_play)
                            .colorRes(R.color.colorAccentLight)
                            .sizeDp(32),
                    null, null, null
            );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.action_add).setIcon(
                new IconDrawable(this, MaterialCommunityIcons.mdi_plus)
                        .colorRes(R.color.colorAccentLight)
                        .actionBarSize())
                .setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                ((PartyApplication) getApplication()).confirmLogout(this);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrackChanged(Track oldTrack, Track newTrack) {

    }
}
