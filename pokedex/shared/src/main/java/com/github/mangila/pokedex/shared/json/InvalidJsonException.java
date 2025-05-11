package com.github.mangila.pokedex.shared.json;

public class InvalidJsonException extends RuntimeException {

    public static final String EMPTY_DATA_ERROR_MESSAGE = "Invalid json data - empty data";
    public static final String TOKENIZE_ERROR_MESSAGE = "Invalid json data cannot tokenize";
    public static final String PARSE_ERROR_MESSAGE = "Invalid json data cannot parse";

    public InvalidJsonException(String message) {
        super(message);
    }
}
