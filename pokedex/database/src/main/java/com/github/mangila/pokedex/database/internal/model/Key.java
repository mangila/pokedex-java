package com.github.mangila.pokedex.database.internal.model;

import com.github.mangila.pokedex.shared.util.Ensure;

public record Key(String value) {
    public static final Key EMPTY = new Key("EMPTY");

    public Key {
        Ensure.notNull(value, "key value must not be null");
        Ensure.notBlank(value, "key value must not be blank");
    }

    public int length() {
        return value.length();
    }

}
