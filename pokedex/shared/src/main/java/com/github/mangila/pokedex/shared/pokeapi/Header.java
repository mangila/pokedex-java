package com.github.mangila.pokedex.shared.pokeapi;

public record Header(
        String key,
        String value
) {
    public String toHeaderLine() {
        return String.format("%s: %s", key, value);
    }
}
