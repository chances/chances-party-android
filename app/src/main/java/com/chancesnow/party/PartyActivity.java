package com.chancesnow.party;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private View mLandingView;
    private View mContentView;
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

        mLandingView = findViewById(R.id.queue_landing);
        Button selectPlaylist = (Button) findViewById(R.id.party_selectPlaylist);
        if (selectPlaylist != null) {
            selectPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectPlaylist();
                }
            });
        }

        mContentView = findViewById(R.id.party_content);

        // TODO: Move this to the player fragment
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
            // TODO: Update a label in the Now Playing page instead
            // R.string.party_playlist, mPlaylistIntent.name

            mLoadingView.setVisibility(View.GONE);
            mLandingView.setVisibility(View.GONE);
            mContentView.setVisibility(View.VISIBLE);
//            getFragmentManager().beginTransaction().show(mPlayerFragment).commit();
        } else {
            mLoadingView.setVisibility(View.GONE);
            mLandingView.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PartyApplication.PICK_PLAYLIST_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                PlaylistSimple playlist = data.getParcelableExtra(STATE_PLAYLIST);
                if (playlist != null) {
                    restoredFromState = false;

                    mPlaylistIntent = playlist;

                    mLandingView.setVisibility(View.GONE);
                    getFragmentManager().beginTransaction().hide(mQueueFragment).commit();

                    // TODO: Do shit with the playlist
                }
            }
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

        if (playlistJson != null && !playlistJson.equals("null")) {
            PlaylistSimple playlist = mGson.fromJson(playlistJson, PlaylistSimple.class);
            restoredFromState = playlist != null;

            mPlaylistIntent = restoredFromState ? playlist : null;
        } else
            selectPlaylist();
    }

    private void selectPlaylist() {
        Intent pickPlaylistIntent = new Intent(this, PlaylistsActivity.class);
        pickPlaylistIntent.setAction(Intent.ACTION_PICK);
        pickPlaylistIntent.putExtra(PlaylistsActivity.STATE_FIRST_TIME, true);
        startActivityForResult(pickPlaylistIntent, PartyApplication.PICK_PLAYLIST_REQUEST);
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
            }
        }
    }
}
