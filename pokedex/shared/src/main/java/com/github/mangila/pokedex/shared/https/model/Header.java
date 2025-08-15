package com.github.mangila.pokedex.shared.https.model;

public record Header(
        String key,
        String value
) {
    public String toRawHeader() {
        return String.format("%s: %s", key, value);
    }
}
