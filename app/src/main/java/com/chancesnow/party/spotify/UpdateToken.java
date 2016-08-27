package com.chancesnow.party.spotify;

public class UpdateToken {
    private String token;
    private long expires;

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
