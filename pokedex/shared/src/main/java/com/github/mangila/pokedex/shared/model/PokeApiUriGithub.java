package com.github.mangila.pokedex.shared.model;

import java.net.URI;
import java.util.Objects;

public record PokeApiUriGithub(URI uri) {

    public PokeApiUriGithub {
        Objects.requireNonNull(uri);
        ensureFromPokeApi(uri);
    }

    public static PokeApiUriGithub fromString(String uri) {
        return new PokeApiUriGithub(URI.create(uri));
    }

    public String getPath() {
        return uri.getPath();
    }

    private void ensureFromPokeApi(URI uri) {
        if (!uri.getHost().equals("raw.githubusercontent.com")) {
            throw new IllegalArgumentException("Host name must be 'raw.githubusercontent.com' - " + uri.getHost());
        }
        if (!uri.getPath().startsWith("/PokeApi")) {
            throw new IllegalArgumentException("Path must start with '/PokeApi' - " + uri.getPath());
        }
    }
}
