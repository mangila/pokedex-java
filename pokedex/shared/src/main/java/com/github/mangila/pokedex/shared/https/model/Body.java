package com.github.mangila.pokedex.shared.https.model;

public record Body(byte[] value) {

    public static Body from(byte[] value) {
        return new Body(value);
    }

}
