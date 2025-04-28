package com.github.mangila.pokedex.shared.https;

public record Header(
        String key,
        String value
) {
    public String toHeaderLine() {
        return key + ": " + value;
    }
}
