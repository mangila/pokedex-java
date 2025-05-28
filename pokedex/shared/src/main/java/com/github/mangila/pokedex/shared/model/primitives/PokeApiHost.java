package com.github.mangila.pokedex.shared.model.primitives;

import java.util.Objects;

/**
 * Domain Primitive with Validation
 */
public record PokeApiHost(String host, int port) {

    public PokeApiHost {
        Objects.requireNonNull(host, "host must not be null");
        ensurePokeApiHost(host);
    }

    public static PokeApiHost fromDefault() {
        return new PokeApiHost("pokeapi.co", 443);
    }

    private void ensurePokeApiHost(String host) {
        if (!(Objects.equals(host, "pokeapi.co") || Objects.equals(host, "raw.githubusercontent.com"))) {
            throw new IllegalArgumentException("Only pokeapi.co and raw.githubusercontent.com are supported as hosts");
        }
    }
}
