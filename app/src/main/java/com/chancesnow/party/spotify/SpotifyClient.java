package com.chancesnow.party.spotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.client.Response;

public class SpotifyClient {
    public static final String TOKEN_EXPIRED = "expired";

    private static final String PREFS_GENERAL = "PartyPrefs";
    private static final String STATE_TOKEN = "spotifyApiToken";
    private static final String STATE_TOKEN_EXPIRES = "spotifyApiTokenExpires";

    private static SpotifyClient ownInstance = new SpotifyClient(TOKEN_EXPIRED, 0);

    private Context mContext;

    private String mSpotifyApiToken;
    private Calendar mDateCalendar;
    private Date mSpotifyApiTokenExpires;

    private SpotifyApi api;
    private SpotifyService spotify;

    public static SpotifyClient getInstance(Context context) {
        ownInstance.mContext = context;

        return ownInstance;
    }

    private SpotifyClient(String accessToken, long expires) {
        mDateCalendar = Calendar.getInstance();

        updateToken(accessToken, expires);

        api = new SpotifyApi();
        api.setAccessToken(accessToken);

        spotify = api.getService();
    }

    public String getToken() {
        return mSpotifyApiToken;
    }

    public Date getTokenExpirationDate() {
        return mSpotifyApiTokenExpires;
    }

    public long getTokenExpirationDateTimestamp() {
        mDateCalendar.setTime(mSpotifyApiTokenExpires);

        return mDateCalendar.getTimeInMillis();
    }

    public boolean isTokenExpired() {
        Date now = new Date();

        return mSpotifyApiToken.equalsIgnoreCase(TOKEN_EXPIRED) ||
                mSpotifyApiTokenExpires.before(now) || mSpotifyApiTokenExpires.equals(now);
    }

    /**
     * Update the Spotify API token and expiration time.
     * @param token Spotify API token
     * @param expires Expiration time, in milliseconds
     * @return True if the token is valid and not expired, false otherwise
     */
    public boolean updateToken(String token, long expires) {
        if (token != null) {
            // If the token isn't expired, restore login state
            if (!token.equalsIgnoreCase(TOKEN_EXPIRED)) {
                mSpotifyApiToken = token;
                mSpotifyApiTokenExpires = new Date(expires);

                api.setAccessToken(mSpotifyApiToken);

                return true;
            }
        }

        return false;
    }

    public void expireToken() {
        mSpotifyApiToken = TOKEN_EXPIRED;
        mSpotifyApiTokenExpires = new Date();

        saveToken();
    }

    /**
     * Load the Spotify API token and expiration date from persistent storage.
     * @return True if the token is valid and not expired, false otherwise
     */
    public boolean loadToken() {
        SharedPreferences state = mContext.getSharedPreferences(PREFS_GENERAL, 0);
        String token = state.getString(STATE_TOKEN, null);

        return token != null &&
                updateToken(
                        token,
                        state.getLong(STATE_TOKEN_EXPIRES, mDateCalendar.getTimeInMillis())
                );
    }

    /**
     * Save the Spotify API token and expiration date to persistent storage.
     */
    public void saveToken() {
        SharedPreferences state = mContext.getSharedPreferences(PREFS_GENERAL, 0);
        SharedPreferences.Editor stateEditor = state.edit();

        mDateCalendar.setTime(mSpotifyApiTokenExpires);
        if (mSpotifyApiTokenExpires.after(new Date())) {
            stateEditor.putString(STATE_TOKEN, mSpotifyApiToken);
            stateEditor.putLong(STATE_TOKEN_EXPIRES, mDateCalendar.getTimeInMillis());
        } else {
            // Persist expired state
            stateEditor.putString(STATE_TOKEN, TOKEN_EXPIRED);
        }

        stateEditor.apply();
    }

    public SpotifyService getSpotify() {
        return spotify;
    }

    public void getOwnPlaylists(final OwnPlaylistsCallback callback) {
        spotify.getMyPlaylists(new SpotifyCallback<Pager<PlaylistSimple>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                callback.failure(spotifyError);
            }

            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                callback.success(playlistSimplePager);
            }
        });
    }

    public void getOwnPlaylists(final OwnPlaylistsCallback callback, int offset) {
        Map<String, Object> options = new ArrayMap<>();
        options.put("offset", offset);

        spotify.getMyPlaylists(options, new SpotifyCallback<Pager<PlaylistSimple>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                callback.failure(spotifyError);
            }

            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                callback.success(playlistSimplePager);
            }
        });
    }

    public void getMe(final MeCallback callback) {
        spotify.getMe(new SpotifyCallback<UserPrivate>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                callback.failure(spotifyError);
            }

            @Override
            public void success(UserPrivate userPrivate, Response response) {
                callback.success(userPrivate);
            }
        });
    }

    public interface Callback {
        void failure(SpotifyError spotifyError);
    }

    public interface OwnPlaylistsCallback extends Callback {
        void success(Pager<PlaylistSimple> playlists);
    }

    public interface MeCallback extends Callback {
        void success(UserPrivate user);
    }
}
