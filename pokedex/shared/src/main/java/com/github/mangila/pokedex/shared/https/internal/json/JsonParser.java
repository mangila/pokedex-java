package com.github.mangila.pokedex.shared.https.internal.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

public class JsonParser {

    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);

    public Object parse(Queue<JsonToken> tokens) {
        if (tokens.isEmpty()) {
            throw new InvalidJsonException(InvalidJsonException.EMPTY_DATA_ERROR_MESSAGE);
        }
        JsonValidator.ensureValidParentheses(tokens);
        var tree = new JsonTree();

        return new Object();
    }
}
