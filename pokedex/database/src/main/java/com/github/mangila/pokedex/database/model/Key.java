package com.github.mangila.pokedex.database.model;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.charset.Charset;

public record Key(String value) {

    public static final Key EMPTY = new Key("EMPTY");
    public static final short MAGIC_NUMBER = 1;

    public Key {
        Ensure.notNull(value, "key entry must not be null");
        Ensure.notBlank(value, "key entry must not be blank");
        Ensure.min(2, value.length());
        Ensure.max(100, value.length());
    }

    public int length() {
        return getBytes().length;
    }

    public int bufferLength() {
        return Short.BYTES + Integer.BYTES + length();
    }

    public byte[] getBytes() {
        return value.getBytes(Charset.defaultCharset());
    }
}
