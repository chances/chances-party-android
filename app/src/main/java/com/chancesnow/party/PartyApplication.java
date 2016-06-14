package com.chancesnow.party;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;

import com.chancesnow.party.spotify.SpotifyClient;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;

import java.util.Calendar;
import java.util.Date;

public class PartyApplication extends Application {

    public static final String PREFS_GENERAL = "PartyPrefs";
    public static final LinearLayout.LayoutParams WRAP_CONTENT_LAYOUT = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    public static final LinearLayout.LayoutParams ZERO_LAYOUT = new LinearLayout.LayoutParams(0, 0, 0);
    public static final LinearLayout.LayoutParams FLEX_LAYOUT = new LinearLayout.LayoutParams(0, 0, 1);

    private SpotifyClient mSpotify;

    @Override
    public void onCreate() {
        super.onCreate();

        Iconify.with(new MaterialCommunityModule());

        mSpotify = SpotifyClient.getInstance(getApplicationContext());
    }

    public SpotifyClient getSpotifyClient() {
        return mSpotify;
    }

    public void confirmLogout(final Activity activityContext) {
        new AlertDialog.Builder(activityContext)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton(getString(R.string.logout), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logout(activityContext);
                }

            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    public void logout(Activity activityContext) {
        dumpUserData();

        activityContext.startActivity(new Intent(activityContext, MainActivity.class));
    }

    public void dumpUserData() {
        mSpotify.expireToken();

        SharedPreferences state = getSharedPreferences(PREFS_GENERAL, 0);
        SharedPreferences.Editor stateEditor = state.edit();

        stateEditor.putString(SpotifyClient.STATE_TOKEN, SpotifyClient.TOKEN_EXPIRED);
        stateEditor.putBoolean(PlaylistsActivity.STATE_PLAYLIST_LOADED, false);
        stateEditor.putString(QueueActivity.STATE_PLAYLIST, null);
        stateEditor.putString(PlaylistsFragment.STATE_PLAYLISTS, null);

        stateEditor.apply();
    }

    public void openWebPage(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
