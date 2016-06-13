package com.chancesnow.party;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;

import kaaes.spotify.webapi.android.models.Track;

public class QueueActivity extends AppCompatActivity
        implements PlayerFragment.OnPlayerInteractionListener {

    private boolean mIsSearching;
    private boolean mIsSearchQueryFocused;

    private View mQueueActivity;
    private Toolbar mToolbar;
    private SearchView mSearchView;

    private View mLoadingView;
    private Button mShuffleButton;
    private PlayerFragment mPlayerFragment;
    private View mFooterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_queue);

        mIsSearching = false;
        mIsSearchQueryFocused = false;

        mQueueActivity = findViewById(R.id.queue);

        mToolbar = (Toolbar) findViewById(R.id.queue_toolbar);
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

        mFooterView = findViewById(R.id.footer);

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
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new SearchActionExpandListener());

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextFocusChangeListener(new SearchQueryTextFocusChangeListener());

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
            case R.id.action_search:
                setSearchState(true);

                return true;
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

    private void setSearchState(boolean searching) {
        mIsSearching = searching;

        if (searching) {
            mLoadingView.setVisibility(View.VISIBLE);
            getFragmentManager().beginTransaction().hide(mPlayerFragment).commit();
            mFooterView.setVisibility(View.GONE);

            mSearchView.requestFocus();
        } else {
            mLoadingView.setVisibility(View.GONE);
            getFragmentManager().beginTransaction().show(mPlayerFragment).commit();
            mFooterView.setVisibility(View.VISIBLE);
        }
    }

    private class SearchActionExpandListener implements MenuItemCompat.OnActionExpandListener {
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            setSearchState(false);

            return true;
        }
    }

    private class SearchQueryTextFocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            mIsSearchQueryFocused = hasFocus;

            if (hasFocus) {
                final View queryTextView = view.findFocus();
                final InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                mSearchView.post(new Runnable() {
                    @Override
                    public void run() {
                        keyboard.showSoftInput(queryTextView, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        }
    }
}
