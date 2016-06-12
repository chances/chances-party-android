package com.chancesnow.party.spotify;

import android.util.ArrayMap;

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
    private static SpotifyClient ownInstance;

    private SpotifyApi api;
    private SpotifyService spotify;

    public static SpotifyClient getInstance(String apiToken) {
        return ownInstance = new SpotifyClient(apiToken);
    }

    private SpotifyClient(String accessToken) {
        api = new SpotifyApi();
        api.setAccessToken(accessToken);

        spotify = api.getService();
    }

    public void refreshToken(String apiToken) {
        api.setAccessToken(apiToken);
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
