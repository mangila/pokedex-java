package com.github.mangila.pokedex.database.model;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.charset.Charset;

public record Field(String value) {
    public static final Field EMPTY = new Field("EMPTY");
    public static final short MAGIC_NUMBER = 2;

    public Field {
        Ensure.notNull(value, "field value must not be null");
        Ensure.notBlank(value, "field value must not be blank");
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
