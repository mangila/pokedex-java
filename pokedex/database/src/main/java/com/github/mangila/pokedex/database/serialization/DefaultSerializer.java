package com.github.mangila.pokedex.database.serialization;

import java.math.BigInteger;

public class DefaultSerializer {
    private final BigIntegerSerializer bigIntegerSerializer;
    private final BooleanSerializer booleanSerializer;
    private final StringUtf8Serializer stringUtf8Serializer;

    public DefaultSerializer() {
        this.bigIntegerSerializer = new BigIntegerSerializer();
        this.booleanSerializer = new BooleanSerializer();
        this.stringUtf8Serializer = new StringUtf8Serializer();
    }

    public byte[] serialize(BigInteger bigInteger) {
        return bigIntegerSerializer.serialize(bigInteger);
    }

    public byte[] serialize(Boolean b) {
        return booleanSerializer.serialize(b);
    }

    public byte[] serialize(String s) {
        return stringUtf8Serializer.serialize(s);
    }
}
