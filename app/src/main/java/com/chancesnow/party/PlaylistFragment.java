package com.chancesnow.party;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chancesnow.party.dummy.DummyContent;
import com.chancesnow.party.dummy.DummyContent.DummyItem;

/**
 * A fragment representing a list of Spotify playlists.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPlaylistListListener}
 * interface.
 */
public class PlaylistFragment extends Fragment {

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

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            int mColumnCount = 1;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new PlaylistViewAdapter(DummyContent.ITEMS, mListener));
        }
        return view;
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
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
