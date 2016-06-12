package com.chancesnow.party;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class PlaylistFragment extends Fragment {

    private static final String STATE_PLAYLISTS = "userPlaylists";

    private boolean restoredFromState;
    private Pager<PlaylistSimple> mPlaylists;

    private RecyclerView mRecyclerView;

    private OnPlaylistListListener mListener;

    public PlaylistFragment() {}

    @SuppressWarnings("unused")
    public static PlaylistFragment newInstance(int columnCount) {
        return new PlaylistFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_list, container, false);

        restoredFromState = false;
        mPlaylists = null;

        // Set the adapter
        mRecyclerView = (RecyclerView) view.findViewById(R.id.playlist_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setAdapter(new PlaylistViewAdapter(new ArrayList<PlaylistSimple>(), mListener));

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

    public void loadPlaylists(SpotifyClient spotifyClient) {
        spotifyClient.getOwnPlaylists(new SpotifyClient.OwnPlaylistsCallback() {
            @Override
            public void failure(SpotifyError spotifyError) {
                if (mListener != null)
                    mListener.onPlaylistLoadError(spotifyError);
            }

            @Override
            public void success(Pager<PlaylistSimple> playlists) {
                if (mListener != null)
                    mListener.onPlaylistsLoaded();

                mPlaylists = playlists;
                addPlaylists(playlists.items);

                // TODO: Handle paging?
            }
        });
    }

    private void clearList() {
        PlaylistViewAdapter adapter = (PlaylistViewAdapter) mRecyclerView.getAdapter();
        int count = adapter.getItemCount();
        adapter.getPlaylists().clear();

        adapter.notifyItemRangeRemoved(0, count);
    }

    private void addPlaylist(PlaylistSimple playlist) {
        PlaylistViewAdapter adapter = (PlaylistViewAdapter) mRecyclerView.getAdapter();
        adapter.getPlaylists().add(playlist);

        adapter.notifyItemInserted(adapter.getItemCount() - 1);
    }

    private void addPlaylists(List<PlaylistSimple> playlists) {
        PlaylistViewAdapter adapter = (PlaylistViewAdapter) mRecyclerView.getAdapter();
        int count = adapter.getItemCount();
        adapter.getPlaylists().addAll(playlists);

        adapter.notifyItemRangeInserted(count - 1, playlists.size());
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
        void onPlaylistLoadError(SpotifyError spotifyError);
        void onPlaylistsLoaded();
        void onPlaylistSelected(PlaylistSimple item);
    }
}
