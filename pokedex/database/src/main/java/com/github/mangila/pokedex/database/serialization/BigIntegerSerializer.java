package com.github.mangila.pokedex.database.serialization;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.math.BigInteger;

final class BigIntegerSerializer implements Serializer<BigInteger> {
    @Override
    public byte[] serialize(BigInteger bigInteger) {
        Ensure.notNull(bigInteger, () -> new SerializationException("Cannot serialize null BigInteger"));
        return bigInteger.toByteArray();
    }

    @Override
    public BigInteger deserialize(byte[] bytes) {
        Ensure.notNull(bytes, () -> new SerializationException("Cannot deserialize null bytes"));
        try {
            return new BigInteger(bytes);
        } catch (NumberFormatException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }
}
