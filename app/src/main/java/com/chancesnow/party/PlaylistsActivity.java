package com.chancesnow.party;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.chancesnow.party.spotify.SpotifyClient;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;

import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class PlaylistsActivity extends AppCompatActivity
        implements PlaylistsFragment.OnPlaylistListListener {

    public static final String STATE_FIRST_TIME = "selectionFirstTime";
    public static final String STATE_PLAYLISTS_LOADED = "playlistLoaded";
    public static final String STATE_SELECTED_PLAYLIST = "selectedPlaylist";

    private boolean mSelectionFirstTime;
    private boolean mPlaylistsLoaded;
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

        mSelectionFirstTime = false;
        mPlaylistsLoaded = false;
        mSpotify = ((PartyApplication) getApplication()).getSpotifyClient();

        mPlaylistsActivity = findViewById(R.id.playlists);

        mToolbar = (Toolbar) findViewById(R.id.playlists_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        mLoadingView = findViewById(R.id.playlists_loading);
        mLayoutView = findViewById(R.id.playlists_layout);

        getFragmentManager().beginTransaction().hide(mPlaylistsFragment).commit();

        // TODO: Prevent the landscape orientation on phone-sized devices.

        if (getIntent() != null && getIntent().getExtras() != null) {
            mSelectionFirstTime = getIntent().getExtras().getBoolean(STATE_FIRST_TIME, false);
        }

        if (mSelectionFirstTime) {
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if (savedInstanceState != null) {
            mPlaylistsLoaded = savedInstanceState.getBoolean(STATE_PLAYLISTS_LOADED, false);
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

        if (mPlaylistsLoaded)
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
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_PLAYLISTS_LOADED, mPlaylistsLoaded);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void respondWithResult(PlaylistSimple selectedPlaylist) {
        mPlaylistsLoaded = true;

        Intent returnIntent = new Intent();
        returnIntent.putExtra(STATE_SELECTED_PLAYLIST, selectedPlaylist);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
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

        // TODO: Don't do this after onSaveInstanceState, crashes app...
        // java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        getFragmentManager().beginTransaction().show(mPlaylistsFragment).commit();
    }

    @Override
    public void onPlaylistSelected(PlaylistSimple item) {
        mPlaylistsFragment.savePlaylists();

        // Navigate to queue
        respondWithResult(item);
    }
}
