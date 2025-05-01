package com.github.mangila.pokedex.shared.https.model;

import java.util.Objects;

public record PokeApiHost(String hostName, int port) {

    public PokeApiHost {
        Objects.requireNonNull(hostName, "hostName must not be null");
        ensurePokeApiHost(hostName);
    }

    private void ensurePokeApiHost(String host) {
        if (!(Objects.equals(host, "pokeapi.co") || Objects.equals(host, "raw.githubusercontent.com"))) {
            throw new IllegalArgumentException("Only pokeapi.co and raw.githubusercontent.com are supported as hosts");
        }
    }
}
