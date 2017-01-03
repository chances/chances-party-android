package com.chancesnow.party.middleware;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.chancesnow.party.AppAction;
import com.chancesnow.party.GsonAdaptersState;
import com.chancesnow.party.ImmutableState;
import com.chancesnow.party.State;

import com.chancesnow.jedux.Action;
import com.chancesnow.jedux.Store;

public class PersistenceController implements Store.Middleware<Action<AppAction, ?>, State> {

    private final SharedPreferences mPreferences;
    private final Gson mGson;

    public PersistenceController(Context c) {
        mPreferences = c.getSharedPreferences("data", 0);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersState());
        mGson = gsonBuilder.create();
    }

    public State getSavedState() {
        if (mPreferences.contains("data")) {
            String json = mPreferences.getString("data", "");
            try {
                return mGson.fromJson(json, ImmutableState.class);
            } catch (Exception ignored) {}
        }
        return null;
    }

    @Override
    public void dispatch(Store<Action<AppAction, ?>, State> store, Action<AppAction, ?> action,
                         Store.NextDispatcher<Action<AppAction, ?>> next) {
        next.dispatch(action);

        String json = mGson.toJson(store.getState());
        mPreferences.edit().putString("data", json).apply();
    }
}
