package com.chancesnow.party;

import kaaes.spotify.webapi.android.models.PlaylistSimple;

public interface PlaylistsFragmentListener {
    void onPlaylistSelected(PlaylistSimple item);
}
