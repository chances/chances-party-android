package com.chancesnow.party;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnPlayerInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends Fragment {
    private static final String ARG_TOKEN = "mSpotifyApiToken";
    private static final String ARG_TOKEN_EXPIRES = "mSpotifyApiTokenExpires";

    private String mSpotifyApiToken;
    private Calendar mDateCalendar;
    private Date mSpotifyApiTokenExpires;

    private OnPlayerInteractionListener mListener;

    public PlayerFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param token Spotify API token
     * @param expires Expiration date of Spotify API token (unix timestamp)
     * @return A new instance of fragment PlayerFragment.
     */
    public static PlayerFragment newInstance(String token, long expires) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();

        args.putString(ARG_TOKEN, token);
        args.putLong(ARG_TOKEN_EXPIRES, expires);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mSpotifyApiToken = getArguments().getString(ARG_TOKEN);
            mSpotifyApiTokenExpires = new Date(getArguments().getLong(ARG_TOKEN_EXPIRES));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onRequestIsTokenExpired();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayerInteractionListener) {
            mListener = (OnPlayerInteractionListener) context;
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

    public void setSpotifyApiToken(String token) {
        this.mSpotifyApiToken = token;
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
    public interface OnPlayerInteractionListener {
        boolean onRequestIsTokenExpired();
    }
}
