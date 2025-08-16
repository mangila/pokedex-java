package com.github.mangila.pokedex.shared.json;

public class InvalidJsonException extends RuntimeException {
    public InvalidJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidJsonException(String message) {
        super(message);
    }
}
