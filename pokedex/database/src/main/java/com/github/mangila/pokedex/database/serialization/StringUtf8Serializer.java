package com.github.mangila.pokedex.database.serialization;

import com.github.mangila.pokedex.shared.util.Ensure;

import static java.nio.charset.StandardCharsets.UTF_8;

final class StringUtf8Serializer implements Serializer<String> {
    @Override
    public byte[] serialize(String s) {
        Ensure.notNull(s, () -> new SerializationException("Cannot serialize null String"));
        return s.getBytes(UTF_8);
    }

    @Override
    public String deserialize(byte[] bytes) {
        Ensure.notNull(bytes, () -> new SerializationException("Cannot deserialize null bytes"));
        return new String(bytes, UTF_8);
    }
}
