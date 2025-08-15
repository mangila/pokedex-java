package com.github.mangila.pokedex.api.client;

import com.github.mangila.pokedex.shared.https.model.GetRequest;

import java.net.URI;
import java.util.Objects;

public record PokeApiUri(URI uri) {

    public PokeApiUri {
        Objects.requireNonNull(uri);
        ensureHost(uri.getHost());
        ensurePath(uri.getPath());
    }

    public static PokeApiUri fromString(String uri) {
        return new PokeApiUri(URI.create(uri));
    }

    public String getPath() {
        return uri.getPath();
    }

    public GetRequest toGetRequest() {
        String query = uri.getQuery();
        String path = uri.getPath();
        if (query != null) {
            path = path + "?" + query;
        }
        return GetRequest.from(path);
    }

    public String getHost() {
        return uri.getHost();
    }

    private static void ensureHost(String host) {
        if (!(Objects.equals(host, "pokeapi.co") || Objects.equals(host, "raw.githubusercontent.com"))) {
            throw new IllegalArgumentException("Only pokeapi.co and raw.githubusercontent.com are supported as hosts");
        }
    }

    private static void ensurePath(String path) {
        if (!(path.startsWith("/api/v2") || path.startsWith("/PokeApi"))) {
            throw new IllegalArgumentException("Only /api/v2 and /PokeApi are supported as paths");
        }
    }
}
