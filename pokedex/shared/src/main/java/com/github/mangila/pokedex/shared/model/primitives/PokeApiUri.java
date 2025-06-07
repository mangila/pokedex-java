package com.github.mangila.pokedex.shared.model.primitives;

import java.net.URI;
import java.util.Objects;

/**
 * Domain Primitive with Validation
 */
public record PokeApiUri(URI uri) {

    public PokeApiUri {
        Objects.requireNonNull(uri);
        ensureFromPokeApi(uri);
    }

    public static PokeApiUri fromString(String uri) {
        return new PokeApiUri(URI.create(uri));
    }

    public String getPath() {
        return uri.getPath();
    }

    private void ensureFromPokeApi(URI uri) {
        if (!uri.getHost().equals("pokeapi.co")) {
            throw new IllegalArgumentException("Host name must be 'pokeapi.co' - " + uri.getHost());
        }
        if (!uri.getPath().startsWith("/api/v2")) {
            throw new IllegalArgumentException("Path must startOffset with '/api/v2' - " + uri.getPath());
        }
    }
}
