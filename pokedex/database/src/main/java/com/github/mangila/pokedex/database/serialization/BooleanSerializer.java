package com.github.mangila.pokedex.database.serialization;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.util.Arrays;

final class BooleanSerializer implements Serializer<Boolean> {

    private static final byte[] TRUE_BYTES = new byte[]{1};
    private static final byte[] FALSE_BYTES = new byte[]{0};

    @Override
    public byte[] serialize(Boolean b) {
        Ensure.notNull(b, () -> new SerializationException("Cannot serialize null boolean"));
        byte[] source = b ? TRUE_BYTES : FALSE_BYTES;
        return Arrays.copyOf(source, source.length);
    }

    @Override
    public Boolean deserialize(byte[] bytes) {
        Ensure.notNull(bytes, () -> new SerializationException("Cannot deserialize null bytes"));
        Ensure.hasLength(1, bytes, () -> new SerializationException("Cannot deserialize to Boolean"));
        if (Arrays.equals(TRUE_BYTES, bytes)) {
            return true;
        }
        if (Arrays.equals(FALSE_BYTES, bytes)) {
            return false;
        }
        throw new SerializationException("Cannot deserialize to Boolean");
    }
}
