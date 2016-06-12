package com.chancesnow.party;

import android.app.Application;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;

public class PartyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Iconify.with(new MaterialCommunityModule());
    }
}
