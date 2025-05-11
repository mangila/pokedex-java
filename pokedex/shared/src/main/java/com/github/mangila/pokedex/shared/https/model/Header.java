package com.github.mangila.pokedex.shared.https.model;

public record Header(
        String key,
        String value
) {
    public String toHeaderLine() {
        return String.format("%s: %s", key, value);
    }
}
