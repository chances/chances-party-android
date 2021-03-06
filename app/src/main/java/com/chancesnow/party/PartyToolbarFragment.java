package com.chancesnow.party;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;

public class PartyToolbarFragment extends Fragment {

    public static final String STATE_SEARCHING = "isSearching";
    public static final String STATE_SEARCH_QUERY = "searchQuery";

    private boolean restoredFromState;
    private boolean mIsSearching;
    private String mSearchQuery;
    private boolean mIsSearchQueryFocused;
    private OnQueueToolbarStateChangeListener mListener;

    private Toolbar mToolbar;
    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;

    public PartyToolbarFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnQueueToolbarStateChangeListener) {
            mListener = (OnQueueToolbarStateChangeListener) context;
            mListener.onQueueToolbarAttached(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnQueueToolbarStateChangeListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mIsSearching = false;
        mIsSearchQueryFocused = false;

        View view = inflater.inflate(R.layout.fragment_party_toolbar, container, false);

        setHasOptionsMenu(true);

        mToolbar = (Toolbar) view.findViewById(R.id.queue_toolbar);

        // Restore previous state if available
        if (savedInstanceState != null) {
            mIsSearching = savedInstanceState.getBoolean(STATE_SEARCHING, false);
            mSearchQuery = savedInstanceState.getString(STATE_SEARCH_QUERY, "");

            restoredFromState = true;
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        activity.setSupportActionBar(mToolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);

        mSearchMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(mSearchMenuItem, new SearchActionExpandListener());

        SearchManager searchManager =
                (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) mSearchMenuItem.getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName())
        );
        int imeOptions = mSearchView.getImeOptions();
        mSearchView.setImeOptions(
                imeOptions | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN
        );
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextFocusChangeListener(new SearchQueryTextFocusChangeListener());

        mSearchMenuItem.setIcon(
                new IconDrawable(getContext(), MaterialCommunityIcons.mdi_plus)
                        .colorRes(R.color.colorAccentLight)
                        .actionBarSize())
                .setTitle(R.string.add_to_queue)
                .setVisible(true);

        if (restoredFromState && mIsSearching)
            enterSearchState(mSearchQuery, true, false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                enterSearchState("", true, false);

                return true;
            case R.id.action_logout:
                ((PartyApplication) getActivity().getApplication()).confirmLogout(getActivity());

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_SEARCHING, mIsSearching);

        mSearchQuery = mSearchView.getQuery().toString();
        savedInstanceState.putString(STATE_SEARCH_QUERY, mSearchQuery);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public boolean isSearching() {
        return mIsSearching;
    }

    public boolean isSearchQueryFocused() {
        return mIsSearchQueryFocused;
    }

    public void enterSearchState(String query, boolean focus, boolean submit) {
        if (mSearchMenuItem != null && !mSearchMenuItem.isActionViewExpanded()) {
            if (mListener != null)
                mListener.onSearchStateChange(mIsSearching = true);

            mSearchMenuItem.expandActionView();
            mSearchView.setQuery(query, submit);
            if (focus)
                mSearchView.requestFocus();
            else
                mSearchView.clearFocus();
        }
    }

    public interface OnQueueToolbarStateChangeListener {
        void onQueueToolbarAttached(PartyToolbarFragment fragment);
        void onSearchStateChange(boolean searching);
    }

    private class SearchActionExpandListener implements MenuItemCompat.OnActionExpandListener {
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            if (mListener != null)
                mListener.onSearchStateChange(mIsSearching = false);

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
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

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
