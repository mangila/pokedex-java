package com.github.mangila.pokedex.database.model;

import com.github.mangila.pokedex.shared.util.Ensure;

public record Key(String value) {

    public static final Key EMPTY = new Key("EMPTY");

    public Key {
        Ensure.notNull(value, "key value must not be null");
        Ensure.notBlank(value, "key value must not be blank");
        Ensure.min(2, value.length());
        Ensure.max(100, value.length());
    }

    public int length() {
        return getBytes().length;
    }

    public byte[] getBytes() {
        return value.getBytes();
    }
}
