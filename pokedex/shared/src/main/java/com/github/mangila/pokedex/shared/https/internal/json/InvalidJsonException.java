package com.github.mangila.pokedex.shared.https.internal.json;

public class InvalidJsonException extends RuntimeException {

    public static final String UNBALANCED_PARENTHESES_ERROR_MESSAGE = "Invalid json data - unbalanced parentheses";
    public static final String EMPTY_DATA_ERROR_MESSAGE = "Invalid json data - empty data";
    public static final String TOKENIZE_ERROR_MESSAGE = "Invalid json data cannot tokenize";
    public static final String PARSE_ERROR_MESSAGE = "Invalid json data cannot parse";

    public InvalidJsonException(String message) {
        super(message);
    }
}
