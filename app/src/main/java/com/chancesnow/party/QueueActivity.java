package com.chancesnow.party;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;

import kaaes.spotify.webapi.android.models.Track;

public class QueueActivity extends AppCompatActivity
        implements QueueToolbarFragment.OnQueueToolbarStateChangeListener,
        PlayerFragment.OnPlayerInteractionListener {

    private View mQueueActivity;
    private QueueToolbarFragment mQueueToolbarFragment;

    private View mLoadingView;
    private Button mShuffleButton;
    private PlayerFragment mPlayerFragment;
    private View mFooterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_queue);

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

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    @Override
    public void onQueueToolbarAttached(QueueToolbarFragment fragment) {
        mQueueToolbarFragment = fragment;
    }

    @Override
    public void onSearchStateChange(boolean searching) {
        if (searching) {
            mLoadingView.setVisibility(View.VISIBLE);
            getFragmentManager().beginTransaction().hide(mPlayerFragment).commit();
            mFooterView.setVisibility(View.GONE);
        } else {
            mLoadingView.setVisibility(View.GONE);
            getFragmentManager().beginTransaction().show(mPlayerFragment).commit();
            mFooterView.setVisibility(View.VISIBLE);
        }
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

            mQueueToolbarFragment.enterSearchState(query, true);
        }
    }
}
