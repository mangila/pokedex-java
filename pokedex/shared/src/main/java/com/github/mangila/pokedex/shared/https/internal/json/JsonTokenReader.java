package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.Queue;

public class JsonTokenReader {

    private final Queue<JsonToken> tokens;

    public JsonTokenReader(Queue<JsonToken> tokens) {
        if (tokens.isEmpty()) {
            throw new InvalidJsonException(InvalidJsonException.EMPTY_DATA_ERROR_MESSAGE);
        }
        JsonValidator.ensureValidParentheses(tokens);
        this.tokens = tokens;
    }

    public JsonToken next() {
        return tokens.poll();
    }

    public JsonToken peek() {
        return tokens.peek();
    }

    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    public JsonToken expect(JsonType type) {
        var token = next();
        if (token == null || token.type() != type) {
            throw new InvalidJsonException(InvalidJsonException.TOKENIZE_ERROR_MESSAGE);
        }
        return token;
    }
}
