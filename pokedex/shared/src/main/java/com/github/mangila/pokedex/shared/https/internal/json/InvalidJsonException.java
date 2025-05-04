package com.github.mangila.pokedex.shared.https.internal.json;

public class InvalidJsonException extends RuntimeException {

    public static final String TOKENIZE_ERROR_MESSAGE = "Invalid json data cannot tokenize - invalid character";

    public InvalidJsonException(String message) {
        super(message);
    }
}
