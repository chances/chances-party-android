package com.chancesnow.party;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.chancesnow.party.spotify.SpotifyClient;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int SPOTIFY_AUTH_REQUEST_CODE = 2977; // Tel keys: C-X-S-S
    private static final String CLIENT_ID = "658e37b135ea40bcabd7b3c61c8070f6";
    private static final String REDIRECT_URI = "chancesparty://callback";
    private static final String TOKEN_EXPIRED = "expired";

    private static final String PREFS_GENERAL = "PartyPrefs";

    private static final String STATE_TOKEN = "spotifyApiToken";
    private static final String STATE_TOKEN_EXPIRES = "spotifyApiTokenExpires";

    private boolean mTryingLogin;

    private SpotifyClient mSpotify;

    private View mMainActivity;

    private View mTitle;
    private Button mLoginButton;
    private View mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mSpotify = ((PartyApplication) getApplication()).getSpotifyClient();

        mTryingLogin = false;

        mMainActivity = findViewById(R.id.main);

        mTitle = findViewById(R.id.main_title);

//        getFragmentManager().beginTransaction().add(R.id.main_player, mPlayerFragment).commit();

        mLoginButton = (Button) findViewById(R.id.main_login);
        mLoadingView = findViewById(R.id.loading);
        if (mLoadingView != null)
            mLoadingView.setVisibility(View.GONE);

        Button shuffle = (Button) findViewById(R.id.player_shuffle);
        if (shuffle != null)
            shuffle.setCompoundDrawablesRelative(
                    new IconDrawable(this, MaterialCommunityIcons.mdi_play)
                            .colorRes(R.color.colorAccentLight)
                            .sizeDp(32),
                    null, null, null
            );

        // Restore persisted state if available
        if (mSpotify.loadToken()) {
            setLoginState(true);
            gotoPlaylists();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mSpotify.saveToken();
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
                    setLoginState(mSpotify.updateToken(response.getAccessToken(),
                            now.getTime() + (response.getExpiresIn() * 1000)
                    ));

                    mSpotify.saveToken();

                    gotoPlaylists();

                    break;

                case ERROR:
                    // Handle error response

                    Snackbar snackbar = Snackbar
                            .make(mMainActivity, "Could not login", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    login(mMainActivity);
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

                    Log.d("d", response.getError()); // AUTHENTICATION_SERVICE_UNAVAILABLE ?
                    Log.d("d", response.getCode());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // TODO: Handle other cases
            }
        }
    }

    private void expireTokenAndLogout() {
        mSpotify.expireToken();

        setLoginState(false);
    }

    private void setLoginState(boolean isLoggedIn) {
        if (isLoggedIn) {
            mLoginButton.setVisibility(View.INVISIBLE);
        } else {
            mLoginButton.setVisibility(View.VISIBLE);
            mLoadingView.setVisibility(View.GONE);
        }
    }

    private void gotoPlaylists() {
        mLoadingView.setVisibility(View.VISIBLE);

        // TODO: Start a session with the Party API

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mSpotify.isTokenExpired()) {
                    startActivity(new Intent(MainActivity.this, PlaylistsActivity.class));
                } else
                    expireTokenAndLogout();
            }
        }, 500);
    }

    public void login(View view) {
        mTryingLogin = true;

        mLoginButton.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.VISIBLE);

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
}
