package com.chancesnow.party;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chancesnow.party.spotify.SpotifyClient;

import kaaes.spotify.webapi.android.models.Track;

public class PlayerFragment extends Fragment {

    private SpotifyClient mSpotify;

    private OnPlayerInteractionListener mListener;

    public PlayerFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onTrackChanged(null, null);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayerInteractionListener) {
            mListener = (OnPlayerInteractionListener) context;
            mListener.onPlayerAttached(this);

            mSpotify = ((PartyApplication) getActivity().getApplication()).getSpotifyClient();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayerInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnPlayerInteractionListener {
        void onPlayerAttached(PlayerFragment fragment);
        void onTrackChanged(Track oldTrack, Track newTrack);
    }
}
