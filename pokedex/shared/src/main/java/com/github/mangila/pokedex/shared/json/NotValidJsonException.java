package com.github.mangila.pokedex.shared.json;

public class NotValidJsonException extends RuntimeException {

    public NotValidJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotValidJsonException(String message) {
        super(message);
    }
}
