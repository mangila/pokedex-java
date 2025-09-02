package com.github.mangila.pokedex.database.serialization;

public class SerializationException extends RuntimeException {
    public SerializationException(String message, Throwable e) {
        super(message, e);
    }

    public SerializationException(String message) {
        super(message);
    }
}
