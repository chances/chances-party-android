package com.chancesnow.party.spotify;

import android.app.Activity;
import android.util.ArrayMap;

import com.chancesnow.party.App;
import com.chancesnow.party.AppAction;
import com.chancesnow.party.State;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.client.Response;

import com.chancesnow.jedux.Action;
import com.chancesnow.jedux.Store;

import static com.chancesnow.party.spotify.SpotifyState.TOKEN_EXPIRED;

public class SpotifyClient implements Store.Middleware<Action<AppAction, ?>, State> {
    public static final int SPOTIFY_AUTH_REQUEST_CODE = 2977; // Tel keys: C-X-S-S
    private static final String CLIENT_ID = "658e37b135ea40bcabd7b3c61c8070f6";
    private static final String REDIRECT_URI = "chancesparty://callback";

    public static final int PAGE_SIZE = 20;

    private static SpotifyClient ownInstance = new SpotifyClient();

    private SpotifyApi api;
    private SpotifyService spotify;

    public static SpotifyClient getInstance() {
        return ownInstance;
    }

    private SpotifyClient() {
        api = new SpotifyApi();
        api.setAccessToken(TOKEN_EXPIRED);

        spotify = api.getService();
    }

    public static String getLargestImage(List<Image> images, int max) {
        int width = 0, height = 0;
        String url = null;

        if (images.size() > 0) {
            for (Image image: images) {
                if (image != null && image.width != null && image.height != null &&
                        image.width <= max && image.height <= max &&
                        (Math.max(width, image.width) == image.width ||
                                Math.max(height, image.height) == image.height)) {
                    width = image.width;
                    height = image.height;
                    url = image.url;
                } else if (image != null && max == Integer.MAX_VALUE) {
                    url = image.url;

                    break;
                }
            }
        }

        return url;
    }

    @Override
    public void dispatch(Store<Action<AppAction, ?>, State> store, Action<AppAction, ?> action,
                         Store.NextDispatcher<Action<AppAction, ?>> next) {
        next.dispatch(action);

        if (action.type == AppAction.LOGIN) {
            Activity activity = (Activity) action.value;

            AuthenticationRequest.Builder builder =
                    new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

            builder.setScopes(new String[]{
                    "user-read-private",
                    "playlist-read-private",
                    "streaming"
            });
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(activity, SPOTIFY_AUTH_REQUEST_CODE, request);
        } else if (action.type == AppAction.UPDATE_TOKEN) {
            String token = App.getState().spotifyState().apiToken();

            if (token != null && !token.equalsIgnoreCase(TOKEN_EXPIRED)) {
                api.setAccessToken(token);
            }
        }
    }

    public SpotifyService getSpotify() {
        return spotify;
    }

    // TODO: Make these requests in a separate threads

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
