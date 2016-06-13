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

    public void logout(Activity activityContext, final View viewContext) {
        new AlertDialog.Builder(activityContext)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton(getString(R.string.logout), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Snackbar.make(viewContext, "Logout in main activity", Snackbar.LENGTH_LONG)
                            .show();

                    mSpotify.expireToken();

                    // TODO: Navigate to main activity with logout intent
                }

            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    public void openWebPage(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
