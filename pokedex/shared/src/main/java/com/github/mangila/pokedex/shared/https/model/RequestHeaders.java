package com.github.mangila.pokedex.shared.https.model;

public final class RequestHeaders extends AbstractHeaders {
    public static final RequestHeaders DEFAULT = RequestHeaders.getDefaultHeaders();
    public static RequestHeaders getDefaultHeaders() {
        RequestHeaders defaultHeaders = new RequestHeaders();
        defaultHeaders.putRaw("Connection", "keep-alive");
        defaultHeaders.putRaw("Accept", "application/json");
        defaultHeaders.putRaw("Accept-Encoding", "gzip");
        defaultHeaders.putRaw("User-Agent", "Pokedex");
        return defaultHeaders;
    }
}
