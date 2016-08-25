package com.chancesnow.party;

import com.chancesnow.party.spotify.SpotifyState;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import trikita.jedux.Action;
import trikita.jedux.Store;

@Value.Immutable
@Value.Enclosing
@Gson.TypeAdapters
public abstract class State {

    public abstract SpotifyState spotifyState();

    public abstract boolean loggedIn();
    public abstract boolean attemptingLogin();

    static class Reducer implements Store.Reducer<Action<AppAction, ?>, State> {
        private ImmutableState copyOf(State state) {
            return ImmutableState.copyOf(state);
        }

        @Override
        public State reduce(Action<AppAction, ?> action, State state) {
            switch (action.type) {
                case LOGIN:
                    return copyOf(state)
                            .withAttemptingLogin(true);
                case UPDATE_TOKEN:
                    SpotifyState.UpdateToken value = (SpotifyState.UpdateToken) action.value;

                    return copyOf(state)
                            .withSpotifyState(
                                    SpotifyState.copyOf(state.spotifyState())
                                    .withApiToken(value.getToken())
                                    .withApiTokenExpirationTimestamp(value.getExpires())
                            );
                case LOGOUT:
                    return copyOf(state)
                            .withSpotifyState(SpotifyState.initial());
            }

            return state;
        }
    }

    static class Default {
        public static State build() {
            return ImmutableState.builder()
                    .spotifyState(SpotifyState.initial())
                    .loggedIn(false)
                    .attemptingLogin(false)
                    .build();
        }
    }
}
