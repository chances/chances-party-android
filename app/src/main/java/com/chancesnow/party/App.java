package com.chancesnow.party;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import com.chancesnow.party.middleware.PersistenceController;
import com.chancesnow.party.spotify.SpotifyClient;
import com.chancesnow.party.ui.MainActivity;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;

import trikita.jedux.Action;
import trikita.jedux.Logger;
import trikita.jedux.Store;

public class App extends Application {

    public static final String PREFS_GENERAL = "PartyPrefs";

    public static final int PICK_PLAYLIST_REQUEST = 1;

    private static App instance;

    private Store<Action<AppAction, ?>, State> store;

    private SpotifyClient mSpotify;

    @Override
    public void onCreate() {
        super.onCreate();
        App.instance = this;

        PersistenceController persistenceController = new PersistenceController(this);
        State initialState = persistenceController.getSavedState();
        if (initialState == null) {
            initialState = State.Default.build();
        }

        mSpotify = SpotifyClient.getInstance();

        this.store = new Store<>(new State.Reducer(), initialState,
                new Logger<>("Party"),
                persistenceController,
                mSpotify);

        Iconify.with(new MaterialCommunityModule());
    }

    public SpotifyClient getSpotifyClient() {
        return mSpotify;
    }

    public static State getState() {
        return instance.store.getState();
    }

    public static State dispatch(Action<AppAction, ?> action) {
        return instance.store.dispatch(action);
    }

    public void confirmLogout(final Activity activityContext) {
        new AlertDialog.Builder(activityContext)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton(getString(R.string.logout), (dialog, which) -> {
                logout(activityContext);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    public void logout(Activity activityContext) {
        App.dispatch(new Action<>(AppAction.LOGOUT));

        activityContext.startActivity(new Intent(activityContext, MainActivity.class));
    }

    public void openWebPage(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
