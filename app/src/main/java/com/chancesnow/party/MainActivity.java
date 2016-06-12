package com.chancesnow.party;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;

import com.chancesnow.party.spotify.SpotifyClient;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.Calendar;
import java.util.Date;

import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class MainActivity extends AppCompatActivity
        implements PlaylistFragment.OnPlaylistListListener, PlayerFragment.OnPlayerInteractionListener {

    private static final int SPOTIFY_AUTH_REQUEST_CODE = 2977; // Tel keys: C-X-S-S
    private static final String CLIENT_ID = "658e37b135ea40bcabd7b3c61c8070f6";
    private static final String REDIRECT_URI = "chancesparty://callback";
    private static final String TOKEN_EXPIRED = "expired";

    private static final String PREFS_GENERAL = "PartyPrefs";

    private static final String STATE_TOKEN = "spotifyApiToken";
    private static final String STATE_TOKEN_EXPIRES = "spotifyApiTokenExpires";

    private static final LinearLayout.LayoutParams WRAP_CONTENT_LAYOUT = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams ZERO_LAYOUT = new LinearLayout.LayoutParams(0, 0, 0);
    private static final LinearLayout.LayoutParams FLEX_LAYOUT = new LinearLayout.LayoutParams(0, 0, 1);

    private boolean mTryingLogin;
    private String mSpotifyApiToken;
    private Calendar mDateCalendar;
    private Date mSpotifyApiTokenExpires;

    private SpotifyClient mSpotify;

    private View mActivityMain;

    private PlaylistFragment mPlaylistsFragment;
    private PlayerFragment mPlayerFragment;
    private LoadingFragment mLoadingFragment;

    private View mTitle;
    private Button mLoginButton;
    private View mAttributionSpace;
    private Button mLogoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTryingLogin = false;
        mSpotifyApiToken = null;
        mDateCalendar = Calendar.getInstance();
        mSpotifyApiTokenExpires = new Date();

        mActivityMain = findViewById(R.id.main);

        mTitle = findViewById(R.id.main_title);

        mPlaylistsFragment = (PlaylistFragment) getFragmentManager().findFragmentById(R.id.main_playlists);
        mPlayerFragment = (PlayerFragment) getFragmentManager().findFragmentById(R.id.main_player);
        mLoadingFragment = (LoadingFragment) getFragmentManager().findFragmentById(R.id.main_loading);

        getFragmentManager().beginTransaction().hide(mLoadingFragment).commit();

        getFragmentManager().beginTransaction().hide(mPlaylistsFragment).commit();
        getFragmentManager().beginTransaction().hide(mPlayerFragment).commit();

//        getFragmentManager().beginTransaction().add(R.id.main_player, mPlayerFragment).commit();

        mLoginButton = (Button) findViewById(R.id.main_login);

        mAttributionSpace = findViewById(R.id.main_attributionSpace);
        mLogoutButton = (Button) findViewById(R.id.main_logout);

        // Restore previous state if available
        if (savedInstanceState != null) {
            String token = savedInstanceState.getString(STATE_TOKEN);

            updateToken(token, savedInstanceState.getLong(STATE_TOKEN_EXPIRES));

            loadPlaylists();

            // TODO: Restore playlists from instance Bundle
        } else {
            // Restore persisted state if available
            SharedPreferences state = getSharedPreferences(PREFS_GENERAL, 0);
            String token = state.getString(STATE_TOKEN, null);
            if (token != null) {
                updateToken(token, state.getLong(STATE_TOKEN_EXPIRES, mDateCalendar.getTimeInMillis()));

                loadPlaylists();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        saveToken();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        // Persist data if token expiration is after now
        mDateCalendar.setTime(mSpotifyApiTokenExpires);
        if (mSpotifyApiTokenExpires.after(new Date())) {
            savedInstanceState.putString(STATE_TOKEN, mSpotifyApiToken);
            savedInstanceState.putLong(STATE_TOKEN_EXPIRES, mDateCalendar.getTimeInMillis());
        } else {
            // Persist expired state
            savedInstanceState.putString(STATE_TOKEN, TOKEN_EXPIRED);
        }

        // TODO: Persist playlists to parcel in Bundle

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == SPOTIFY_AUTH_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            mTryingLogin = false;

            switch (response.getType()) {
                case TOKEN:
                    Date now = new Date();
                    updateToken(response.getAccessToken(),
                            now.getTime() + (response.getExpiresIn() * 1000)
                    );

                    saveToken();

                    loadPlaylists();

                    break;

                case ERROR:
                    // Handle error response

                    Snackbar snackbar = Snackbar
                            .make(mActivityMain, "Could not login", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    login(mActivityMain);
                                }
                            })
                            .setCallback(new Snackbar.Callback() {
                                // Show the login button if the user did not immediately retry
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    super.onDismissed(snackbar, event);

                                    if (mLoginButton != null && !mTryingLogin) {
                                        setLoginState(false);
                                    }
                                }
                            });

                    snackbar.show();

                    Log.d("d", response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // TODO: Handle other cases
            }
        }
    }

    private void updateToken(String token, long expires) {
        if (token != null) {
            // If the token isn't expired, restore login state
            if (!token.equalsIgnoreCase(TOKEN_EXPIRED)) {
                mSpotifyApiToken = token;
                mSpotifyApiTokenExpires = new Date(expires);

                // Update Spotify Web API Client
                if (mSpotify == null) {
                    mSpotify = SpotifyClient.getInstance(mSpotifyApiToken);
                } else {
                    mSpotify.refreshToken(mSpotifyApiToken);
                }

                mPlayerFragment.setSpotifyClient(mSpotify);

                setLoginState(true);
            } else {
                setLoginState(false);
            }
        } else {
            setLoginState(false);
        }
    }

    private void saveToken() {
        SharedPreferences state = getSharedPreferences(PREFS_GENERAL, 0);
        SharedPreferences.Editor stateEditor = state.edit();

        mDateCalendar.setTime(mSpotifyApiTokenExpires);
        if (mSpotifyApiTokenExpires.after(new Date())) {
            stateEditor.putString(STATE_TOKEN, mSpotifyApiToken);
            stateEditor.putLong(STATE_TOKEN_EXPIRES, mDateCalendar.getTimeInMillis());
        } else {
            // Persist expired state
            stateEditor.putString(STATE_TOKEN, TOKEN_EXPIRED);
        }

        stateEditor.apply();
    }

    private void expireTokenAndLogout() {
        mSpotifyApiToken = TOKEN_EXPIRED;
        mSpotifyApiTokenExpires = new Date();

        setLoginState(false);
    }

    private void setLoginState(boolean isLoggedIn) {
        if (isLoggedIn) {

            mTitle.setVisibility(View.INVISIBLE);

            mLoginButton.setVisibility(View.INVISIBLE);

            // Show the logout button
            mLogoutButton.setLayoutParams(WRAP_CONTENT_LAYOUT);
            mLogoutButton.setVisibility(View.VISIBLE);
            mAttributionSpace.setLayoutParams(ZERO_LAYOUT);
        } else {
            mTitle.setVisibility(View.VISIBLE);

            mLoginButton.setVisibility(View.VISIBLE);

            getFragmentManager().beginTransaction().hide(mLoadingFragment).commit();
            getFragmentManager().beginTransaction().hide(mPlayerFragment).commit();

            // Hide the logout button
            mLogoutButton.setLayoutParams(ZERO_LAYOUT);
            mLogoutButton.setVisibility(View.INVISIBLE);
            mAttributionSpace.setLayoutParams(FLEX_LAYOUT);
        }
    }

    private void loadPlaylists() {
        if (mSpotifyApiTokenExpires.after(new Date())) {
            getFragmentManager().beginTransaction().show(mLoadingFragment).commit();

            mLoadingFragment.setLoadingTopic("party shit");

            // TODO: Start a session with the Party API

            mPlaylistsFragment.loadPlaylists(mSpotify);
        } else
            expireTokenAndLogout();
    }

    public void login(View view) {
        mTryingLogin = true;

        mLoginButton.setVisibility(View.INVISIBLE);

        getFragmentManager().beginTransaction().show(mLoadingFragment).commit();

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{
                "user-read-private",
                "playlist-read-private",
                "streaming"
        });
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, SPOTIFY_AUTH_REQUEST_CODE, request);
    }

    public void logout(View view) {

        new AlertDialog.Builder(this)
                .setTitle("Logout from Spotify")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(getString(R.string.logout), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        expireTokenAndLogout();
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onRequestIsTokenExpired() {
        Date now = new Date();

        return mSpotifyApiToken.equalsIgnoreCase(TOKEN_EXPIRED) ||
                mSpotifyApiTokenExpires.before(now) || mSpotifyApiTokenExpires.equals(now);
    }

    @Override
    public void onPlaylistLoadError(SpotifyError spotifyError) {
        // The access token has expired
        if (spotifyError.getRetrofitError().getResponse().getStatus() == 401 &&
                spotifyError.getErrorDetails().message.contains("token expired")) {
            expireTokenAndLogout();
            return;
        }

        Log.d("d", spotifyError.toString());
        Snackbar
                .make(mActivityMain, spotifyError.getErrorDetails().message, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onPlaylistsLoaded() {
        //getFragmentManager().beginTransaction().show(mPlayerFragment).commit();
        getFragmentManager().beginTransaction().hide(mLoadingFragment).commit();
        getFragmentManager().beginTransaction().show(mPlaylistsFragment).commit();
    }

    @Override
    public void onPlaylistSelected(PlaylistSimple item) {

    }
}
