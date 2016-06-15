package com.chancesnow.party;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.chancesnow.party.spotify.SpotifyClient;
import com.google.gson.Gson;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;

import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class PlaylistsActivity extends AppCompatActivity
        implements PlaylistsFragment.OnPlaylistListListener {

    public static final String ACTION_LOAD_PLAYLIST = "queueLoadPlaylist";
    public static final String STATE_PLAYLIST_LOADED = "playlistLoaded";

    private boolean mPlaylistLoaded;
    private SpotifyClient mSpotify;

    private View mPlaylistsActivity;

    private Toolbar mToolbar;
    private MenuItem mNowPlayingMenuItem;

    private View mLoadingView;
    private View mLayoutView;

    private PlaylistsFragment mPlaylistsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playlists);

        mPlaylistLoaded = false;
        mSpotify = ((PartyApplication) getApplication()).getSpotifyClient();

        mPlaylistsActivity = findViewById(R.id.playlists);

        mToolbar = (Toolbar) findViewById(R.id.playlists_toolbar);
        setSupportActionBar(mToolbar);

        mLoadingView = findViewById(R.id.playlists_loading);
        mLayoutView = findViewById(R.id.playlists_layout);

        getFragmentManager().beginTransaction().hide(mPlaylistsFragment).commit();

        if (savedInstanceState != null) {
            mPlaylistLoaded = savedInstanceState.getBoolean(STATE_PLAYLIST_LOADED, false);

            if (mPlaylistLoaded) {
                // Check for saved selected playlist, switching to queue if available
                SharedPreferences state = getSharedPreferences(PartyApplication.PREFS_GENERAL, 0);
                String playlistJson = state.getString(PartyActivity.STATE_PLAYLIST, null);
                if (playlistJson != null) {
                    Gson gson = new Gson();
                    try {
                        PlaylistSimple playlist = gson.fromJson(playlistJson, PlaylistSimple.class);
                        if (playlist != null) {
                            mPlaylistLoaded = true;

                            navigateToQueue(playlist, true);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.action_refresh).setVisible(true);

        mNowPlayingMenuItem = menu.findItem(R.id.action_now_playing).setIcon(
                new IconDrawable(this, MaterialCommunityIcons.mdi_playlist_play)
                        .colorRes(R.color.colorAccentLight)
                        .actionBarSize())
                .setVisible(false);

        if (mPlaylistLoaded)
            mNowPlayingMenuItem.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mPlaylistsFragment.refreshPlaylists();

                return true;
            case R.id.action_now_playing:
                startActivity(new Intent(PlaylistsActivity.this, PartyActivity.class));

                return true;
            case R.id.action_logout:
                ((PartyApplication) getApplication()).confirmLogout(this);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPlaylistLoaded && mNowPlayingMenuItem != null)
            mNowPlayingMenuItem.setVisible(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_PLAYLIST_LOADED, mPlaylistLoaded);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void navigateToQueue(PlaylistSimple selectedPlaylist, boolean instant) {
        mPlaylistLoaded = true;

        Intent intent = new Intent(PlaylistsActivity.this, PartyActivity.class);
        intent.setAction(ACTION_LOAD_PLAYLIST);
        intent.putExtra(PartyActivity.STATE_PLAYLIST, selectedPlaylist);
        startActivity(intent);
        if (instant)
            overridePendingTransition(0, 0); // TODO: Set slide left out and in transition?
    }

    @Override
    public void onAttached(PlaylistsFragment fragment) {
        mPlaylistsFragment = fragment;
    }

    @Override
    public void onPlaylistLoadError(SpotifyError spotifyError) {
        // The access token has expired
        String message = spotifyError.getErrorDetails().message.toLowerCase();
        if (spotifyError.getRetrofitError().getResponse().getStatus() == 401 &&
                (message.contains("token expired")) || message.contains("invalid access token")) {
            getFragmentManager().beginTransaction().hide(mPlaylistsFragment).commit();

            ((PartyApplication) getApplication()).logout(this);

            return;
        }

        final PlaylistsActivity that = this;

        Log.d("d", spotifyError.toString());
        Snackbar.make(mPlaylistsActivity, "Error: " +
                spotifyError.getErrorDetails().message +
                " (" + spotifyError.getErrorDetails().status + ")",
                Snackbar.LENGTH_LONG)
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);

                        ((PartyApplication) getApplication()).logout(that);
                    }
                }).show();
    }

    @Override
    public void onPlaylistsLoaded() {
        mLoadingView.setVisibility(View.GONE);
        mLayoutView.setVisibility(View.VISIBLE);

        getFragmentManager().beginTransaction().show(mPlaylistsFragment).commit();
    }

    @Override
    public void onPlaylistSelected(PlaylistSimple item) {
        mPlaylistsFragment.savePlaylists();

        // Navigate to queue
        navigateToQueue(item, false);
    }
}
