package com.chancesnow.party;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.chancesnow.party.spotify.SpotifyClient;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

/**
 * A fragment representing a list of Spotify playlists.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPlaylistListListener}
 * interface.
 */
public class PlaylistsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_PLAYLISTS = "userPlaylists";

    private SpotifyClient mSpotify;

    private boolean restoredFromState;
    private Pager<PlaylistSimple> mPlaylists;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private OnPlaylistListListener mListener;

    public PlaylistsFragment() {}

    @SuppressWarnings("unused")
    public static PlaylistsFragment newInstance(int columnCount) {
        return new PlaylistsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_list, container, false);

        restoredFromState = false;
        mPlaylists = null;

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.playlist_swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        // Set the adapter
        mRecyclerView = (RecyclerView) view.findViewById(R.id.playlist_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setAdapter(new PlaylistAdapter(new ArrayList<PlaylistSimple>(), mListener));

        mProgressBar = (ProgressBar) view.findViewById(R.id.playlist_progress);
        mProgressBar.setVisibility(View.GONE);

        // Restore previous state if available
        if (savedInstanceState != null) {
            Pager<PlaylistSimple> playlists = savedInstanceState.getParcelable(STATE_PLAYLISTS);
            if (playlists != null) {
                mPlaylists = playlists;

                addPlaylists(playlists.items);

                restoredFromState = true;
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (restoredFromState && mListener != null)
            mListener.onPlaylistsLoaded();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlaylistListListener) {
            mListener = (OnPlaylistListListener) context;
            mListener.onAttached(this);

            mSpotify = ((PartyApplication) getActivity().getApplication()).getSpotifyClient();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlaylistListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mPlaylists != null) {
            savedInstanceState.putParcelable(STATE_PLAYLISTS, mPlaylists);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    public void loadPlaylists() {
        if (mSpotify != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(0);

            mSpotify.getOwnPlaylists(new SpotifyClient.OwnPlaylistsCallback() {
                @Override
                public void failure(SpotifyError spotifyError) {
                    if (mListener != null)
                        mListener.onPlaylistLoadError(spotifyError);
                }

                @Override
                public boolean success(Pager<PlaylistSimple> playlists, int page, int pages) {
                    if (page == 0) {
                        clearList();

                        mPlaylists = playlists;

                        mSwipeRefreshLayout.setRefreshing(false);
                    } else {
                        mPlaylists.items.addAll(playlists.items);
                    }

                    addPlaylists(playlists.items);

                    mProgressBar.setProgress((int) ((page + 1.0) / pages * mProgressBar.getMax()));

                    if (page == pages - 1) {
                        mProgressBar.setVisibility(View.GONE);
                    }

                    if (page == 0) {
                        // Scroll to top of list
                        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView
                                .getLayoutManager();
                        layoutManager.scrollToPositionWithOffset(0, 0);

                        if (mListener != null)
                            mListener.onPlaylistsLoaded();
                    }

                    return page < pages - 1;
                }
            });
        }
    }

    private void clearList() {
        PlaylistAdapter adapter = (PlaylistAdapter) mRecyclerView.getAdapter();
        int count = adapter.getItemCount();
        adapter.getPlaylists().clear();

        adapter.notifyItemRangeRemoved(0, count);
    }

    private void addPlaylist(PlaylistSimple playlist) {
        PlaylistAdapter adapter = (PlaylistAdapter) mRecyclerView.getAdapter();
        adapter.getPlaylists().add(playlist);

        adapter.notifyItemInserted(adapter.getItemCount() - 1);
    }

    private void addPlaylists(List<PlaylistSimple> playlists) {
        PlaylistAdapter adapter = (PlaylistAdapter) mRecyclerView.getAdapter();
        int count = adapter.getItemCount();
        adapter.getPlaylists().addAll(playlists);

        adapter.notifyItemRangeInserted(count - 1, playlists.size());
    }

    @Override
    public void onRefresh() {
        loadPlaylists();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPlaylistListListener {
        void onAttached(PlaylistsFragment self);
        void onPlaylistLoadError(SpotifyError spotifyError);
        void onPlaylistsLoaded();
        void onPlaylistSelected(PlaylistSimple item);
    }
}