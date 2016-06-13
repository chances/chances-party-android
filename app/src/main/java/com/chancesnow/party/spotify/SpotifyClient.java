package com.chancesnow.party.spotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import com.chancesnow.party.PartyApplication;

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
    public static final String STATE_TOKEN = "spotifyApiToken";
    public static final String STATE_TOKEN_EXPIRES = "spotifyApiTokenExpires";

    public static final int PAGE_SIZE = 20;

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
            } else {
                mSpotifyApiToken = TOKEN_EXPIRED;
                mSpotifyApiTokenExpires = new Date();
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
        SharedPreferences state = mContext.getSharedPreferences(PartyApplication.PREFS_GENERAL, 0);
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
        SharedPreferences state = mContext.getSharedPreferences(PartyApplication.PREFS_GENERAL, 0);
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
        getOwnPlaylists(callback, 0, PAGE_SIZE);
    }

    private void getOwnPlaylists(final OwnPlaylistsCallback callback, final int offset, final int limit) {
        final Map<String, Object> options = new ArrayMap<>();
        options.put("offset", offset);

        final SpotifyCallback<Pager<PlaylistSimple>> apiCallback = new SpotifyCallback<Pager<PlaylistSimple>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                callback.failure(spotifyError);
            }

            @Override
            public void success(Pager<PlaylistSimple> playlistPager, Response response) {
                int total = playlistPager.total,
                        pages = (int) Math.ceil((total + 0.0) / limit),
                        page = playlistPager.offset / limit;

                if (callback.success(playlistPager, page, pages) &&
                        playlistPager.offset + limit < total) {
                    options.put("offset", playlistPager.offset + limit);
                    spotify.getMyPlaylists(options, this);
                }
            }
        };

        spotify.getMyPlaylists(options, apiCallback);
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

    public interface PagedCallback<T> extends Callback {
        /**
         * Called when a paged request succeeds.
         * @param response A {@link Pager} paged response
         * @param page Current page of response
         * @param pages Total number of pages
         * @return True if next page should be requested, false otherwise
         */
        boolean success(Pager<T> response, int page, int pages);
    }

    public interface OwnPlaylistsCallback extends PagedCallback<PlaylistSimple> {}

    public interface MeCallback extends Callback {
        void success(UserPrivate user);
    }
}
