package com.chancesnow.party.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.chancesnow.party.App;
import com.chancesnow.party.AppAction;
import com.chancesnow.party.PartyActivity;
import com.chancesnow.party.spotify.SpotifyClient;
import com.chancesnow.party.spotify.UpdateToken;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.Date;

import trikita.jedux.Action;

public class MainActivity extends AppCompatActivity {

    private View mMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainActivity = new com.chancesnow.party.ui.MainLayout(this);

        setContentView(mMainActivity);

        // Try to go to the Party activity
        gotoParty();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == SpotifyClient.SPOTIFY_AUTH_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                case TOKEN:
                    Date now = new Date();
                    App.dispatch(new Action<>(AppAction.UPDATE_TOKEN, new UpdateToken(
                            response.getAccessToken(),
                            now.getTime() + (response.getExpiresIn() * 1000)
                    )));

                    new Handler(Looper.getMainLooper()).postDelayed(this::gotoParty, 750);

                    break;

                case ERROR:
                    // Handle error response

                    Snackbar snackbar = Snackbar
                            .make(mMainActivity, "Could not login", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", v -> {
                                App.dispatch(new Action<>(AppAction.LOGIN, MainActivity.this));
                            })
                            .setCallback(new Snackbar.Callback() {
                                // Show the login button if the user did not immediately retry
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    super.onDismissed(snackbar, event);

                                    App.dispatch(new Action<>(AppAction.LOGOUT));
                                }
                            });

                    snackbar.show();

                    Log.d("d", response.getError()); // AUTHENTICATION_SERVICE_UNAVAILABLE ?
                    Log.d("d", response.getCode());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // TODO: Handle other cases?

                    App.dispatch(new Action<>(AppAction.LOGOUT));
            }
        }
    }

    private void gotoParty() {
        if (!App.getState().spotifyState().isTokenExpired()) {
            // TODO: Start a session with the Party API?

            Intent intent = new Intent(MainActivity.this, PartyActivity.class);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
            );
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (App.getState().loggedIn())
            App.dispatch(new Action<>(AppAction.LOGOUT));
    }
}
