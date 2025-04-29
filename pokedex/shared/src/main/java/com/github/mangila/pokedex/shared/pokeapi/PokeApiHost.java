package com.github.mangila.pokedex.shared.pokeapi;

import java.util.Objects;

public record PokeApiHost(String host) {

    public PokeApiHost {
        Objects.requireNonNull(host, "host must not be null");
        ensurePokeApiHost(host);
    }

    private void ensurePokeApiHost(String host) {
        if (!Objects.equals(host, "pokeapi.co") || !Objects.equals(host, "raw.githubusercontent.com")) {
            throw new IllegalArgumentException("Only pokeapi.co and raw.githubusercontent.com are supported as hosts");
        }
    }
}
