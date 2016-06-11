package com.chancesnow.party;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements PlayerFragment.OnPlayerInteractionListener {

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

    private View mActivityMain;
    private PlayerFragment player;
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

        player = (PlayerFragment) getFragmentManager().findFragmentById(R.id.main_player);

        getFragmentManager().beginTransaction().hide(player).commit();

//        player.setArguments(savedInstanceState);
//        getFragmentManager().beginTransaction().add(R.id.main_player, player).commit();

        mLoginButton = (Button) findViewById(R.id.main_login);

        mAttributionSpace = findViewById(R.id.main_attributionSpace);
        mLogoutButton = (Button) findViewById(R.id.main_logout);

        // Restore previous state if available
        if (savedInstanceState != null) {
            String token = savedInstanceState.getString(STATE_TOKEN);

            updateToken(token, savedInstanceState.getLong(STATE_TOKEN_EXPIRES));
        } else {
            // Restore persisted state if available
            SharedPreferences state = getSharedPreferences(PREFS_GENERAL, 0);
            String token = state.getString(STATE_TOKEN, null);
            if (token != null) {
                updateToken(token, state.getLong(STATE_TOKEN_EXPIRES, mDateCalendar.getTimeInMillis()));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

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

        stateEditor.commit();
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

                player.setSpotifyApiToken(token);

                setLoginState(true);
            } else {
                setLoginState(false);
            }
        } else {
            setLoginState(false);
        }
    }

    private void setLoginState(boolean isLoggedIn) {
        if (isLoggedIn) {

            //DateFormat dateFormat = DateFormat.getTimeInstance();
            // Hello, %1$s! You have %2$d new messages.
            // String.format(R.string.message, foo, bar);

            mLoginButton.setVisibility(View.INVISIBLE);

            getFragmentManager().beginTransaction().show(player).commit();

            // Show the logout button
            mLogoutButton.setLayoutParams(WRAP_CONTENT_LAYOUT);
            mLogoutButton.setVisibility(View.VISIBLE);
            mAttributionSpace.setLayoutParams(ZERO_LAYOUT);
        } else {
            mLoginButton.setVisibility(View.VISIBLE);

            getFragmentManager().beginTransaction().hide(player).commit();

            // Hide the logout button
            mLogoutButton.setLayoutParams(ZERO_LAYOUT);
            mLogoutButton.setVisibility(View.INVISIBLE);
            mAttributionSpace.setLayoutParams(FLEX_LAYOUT);
        }
    }

    public void login(View view) {
        mTryingLogin = true;

        if (mLoginButton != null) {
            mLoginButton.setVisibility(View.INVISIBLE);
        }

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, SPOTIFY_AUTH_REQUEST_CODE, request);
    }

    public void logout(View view) {
        mSpotifyApiToken = TOKEN_EXPIRED;
        mSpotifyApiTokenExpires = new Date();

        setLoginState(false);
    }

    @Override
    public boolean onRequestIsTokenExpired() {
        Date now = new Date();

        return mSpotifyApiToken.equalsIgnoreCase(TOKEN_EXPIRED) ||
                mSpotifyApiTokenExpires.before(now) || mSpotifyApiTokenExpires.equals(now);
    }
}
