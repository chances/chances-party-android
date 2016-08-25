package com.chancesnow.party.spotify;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.Date;

@Value.Immutable
@Gson.TypeAdapters
public abstract class SpotifyState {

    public static final String TOKEN_EXPIRED = "expired";

    public abstract String apiToken();
    public abstract long apiTokenExpirationTimestamp();

    public static ImmutableSpotifyState copyOf(SpotifyState state) {
        return ImmutableSpotifyState.copyOf(state);
    }

    public static SpotifyState initial() {
        return ImmutableSpotifyState.builder()
                .apiToken(TOKEN_EXPIRED)
                .apiTokenExpirationTimestamp(0)
                .build();
    }

    @Value.Auxiliary
    public Date getTokenExpirationDate() {
        return new Date(this.apiTokenExpirationTimestamp());
    }

    @Value.Auxiliary
    public boolean isTokenExpired() {
        Date now = new Date();
        Date expires = new Date(this.apiTokenExpirationTimestamp());

        return this.apiToken().equalsIgnoreCase(TOKEN_EXPIRED) ||
                expires.before(now) || expires.equals(now);
    }

    public class UpdateToken {
        String token;
        long expires;

        public UpdateToken(String token, long expires) {
            this.token = token;
            this.expires = expires;
        }

        public String getToken() {
            return token;
        }

        public long getExpires() {
            return expires;
        }
    }
}
