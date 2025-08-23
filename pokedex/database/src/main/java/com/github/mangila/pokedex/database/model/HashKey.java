package com.github.mangila.pokedex.database.model;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.charset.Charset;

public record HashKey(String value) {

    public static final HashKey EMPTY = new HashKey("EMPTY");
    public static final short MAGIC_NUMBER = 1;

    public HashKey {
        Ensure.notNull(value, "hashkey value must not be null");
        Ensure.notBlank(value, "hashkey value must not be blank");
        Ensure.min(2, value.length());
        Ensure.max(100, value.length());
    }

    public int length() {
        return getBytes().length;
    }

    public int getBufferSize() {
        return Short.BYTES + Integer.BYTES + length();
    }

    public byte[] getBytes() {
        return value.getBytes(Charset.defaultCharset());
    }
}
