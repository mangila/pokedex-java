package com.github.mangila.pokedex.database.serialization;

sealed interface Serializer<T> permits BigIntegerSerializer, BooleanSerializer, StringUtf8Serializer {
    byte[] serialize(T t);

    T deserialize(byte[] bytes);
}
