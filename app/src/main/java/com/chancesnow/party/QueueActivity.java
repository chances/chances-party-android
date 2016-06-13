package com.chancesnow.party;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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
    private PlayerFragment mPlayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_queue);

        mQueueActivity = findViewById(R.id.queue);

        mToolbar = (Toolbar) findViewById(R.id.playlists_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        mLoadingView = findViewById(R.id.queue_loading);
        if (mLoadingView != null)
            mLoadingView.setVisibility(View.GONE);

        mShuffleButton = (Button) findViewById(R.id.player_shuffle);
        if (mShuffleButton != null)
            mShuffleButton.setCompoundDrawablesRelative(
                    new IconDrawable(this, MaterialCommunityIcons.mdi_play)
                            .colorRes(R.color.colorAccentLight)
                            .sizeDp(32),
                    null, null, null
            );

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchMenuItem.setIcon(
                new IconDrawable(this, MaterialCommunityIcons.mdi_plus)
                        .colorRes(R.color.colorAccentLight)
                        .actionBarSize())
                .setTitle(R.string.add_to_queue)
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
    public void onPlayerAttached(PlayerFragment fragment) {
        mPlayerFragment = fragment;
    }

    @Override
    public void onTrackChanged(Track oldTrack, Track newTrack) {

    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            mLoadingView.setVisibility(View.VISIBLE);
            getFragmentManager().beginTransaction().hide(mPlayerFragment).commit();
        }
    }
}
