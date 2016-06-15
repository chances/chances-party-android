package com.chancesnow.party;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;

import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.Track;

public class PartyActivity extends AppCompatActivity
        implements PartyToolbarFragment.OnQueueToolbarStateChangeListener,
        QueueFragment.OnQueueFragmentListener,
        PlayerFragment.OnPlayerInteractionListener {

    public static final String STATE_PLAYLIST = "selectedPlaylist";

    private Gson mGson;
    private boolean restoredFromState;
    private PlaylistSimple mPlaylistIntent;
    private Playlist mPlaylist;

    private View mQueueActivity;
    private PartyToolbarFragment mPartyToolbarFragment;

    private View mLoadingView;
    private QueueFragment mQueueFragment;
    private Button mShuffleButton;
    private PlayerFragment mPlayerFragment;
    private View mFooterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_party);

        mGson = new Gson();

        mQueueActivity = findViewById(R.id.queue);

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

        mFooterView = findViewById(R.id.footer);

        // Restore previous state if available
        if (savedInstanceState != null) {
            PlaylistSimple playlist = savedInstanceState.getParcelable(STATE_PLAYLIST);
            if (playlist != null) {
                mPlaylistIntent = playlist;

                restoredFromState = true;
            }
        } else
            loadPlaylist();

        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPlaylistIntent != null) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setSubtitle(getString(R.string.party_playlist, mPlaylistIntent.name));

            mLoadingView.setVisibility(View.GONE);
            getFragmentManager().beginTransaction().show(mQueueFragment).commit();
            getFragmentManager().beginTransaction().show(mPlayerFragment).commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        savePlaylist();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(STATE_PLAYLIST, mPlaylistIntent);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onQueueToolbarAttached(PartyToolbarFragment fragment) {
        mPartyToolbarFragment = fragment;
    }

    @Override
    public void onSearchStateChange(boolean searching) {
        if (searching) {
            mLoadingView.setVisibility(View.GONE);
            getFragmentManager().beginTransaction().hide(mQueueFragment).commit();
            getFragmentManager().beginTransaction().hide(mPlayerFragment).commit();
            mFooterView.setVisibility(View.GONE);
        } else {
            // TODO: Handle other queue states
            mLoadingView.setVisibility(View.GONE);
            getFragmentManager().beginTransaction().show(mQueueFragment).commit();
            getFragmentManager().beginTransaction().show(mPlayerFragment).commit();
            mFooterView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onQueueAttached(QueueFragment fragment) {
        mQueueFragment = fragment;
    }

    @Override
    public void onPlayerAttached(PlayerFragment fragment) {
        mPlayerFragment = fragment;
    }

    @Override
    public void onTrackChanged(Track oldTrack, Track newTrack) {

    }

    private void loadPlaylist() {
        SharedPreferences state = getSharedPreferences(PartyApplication.PREFS_GENERAL, 0);
        String playlistJson = state.getString(STATE_PLAYLIST, null);

        if (playlistJson != null) {
            PlaylistSimple playlist = mGson.fromJson(playlistJson, PlaylistSimple.class);
            restoredFromState = playlist != null;

            mPlaylistIntent = restoredFromState ? playlist : null;
        }
    }

    private void savePlaylist() {
        SharedPreferences state = getSharedPreferences(PartyApplication.PREFS_GENERAL, 0);
        SharedPreferences.Editor stateEditor = state.edit();

        String playlistJson = mGson.toJson(mPlaylistIntent);
        stateEditor.putString(STATE_PLAYLIST, playlistJson);

        stateEditor.apply();
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case Intent.ACTION_SEARCH:
                    String query = intent.getStringExtra(SearchManager.QUERY);

                    mPartyToolbarFragment.enterSearchState(query, false, false);

                    break;
                case PlaylistsActivity.ACTION_LOAD_PLAYLIST:
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        PlaylistSimple playlist =
                                extras.getParcelable(STATE_PLAYLIST);
                        if (playlist != null) {

                            restoredFromState = false;

                            mPlaylistIntent = playlist;
                        }
                    }

                    break;
            }
        }
    }
}
