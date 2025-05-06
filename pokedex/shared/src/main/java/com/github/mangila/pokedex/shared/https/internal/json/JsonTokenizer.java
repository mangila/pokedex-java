package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.*;

public class JsonTokenizer {

    public Queue<JsonToken> tokenizeFrom(byte[] data) {
        return tokenize(data);
    }

    public Queue<JsonToken> tokenizeFrom(String data) {
        return tokenize(data.getBytes());
    }

    private Queue<JsonToken> tokenize(byte[] data) {
        Objects.requireNonNull(data, "json data must not be null");
        var lexer = new JsonLexer(data);
        var tokens = new ArrayDeque<JsonToken>();
        while (!lexer.finishedReadingData()) {
            var current = lexer.read();
            if (Character.isWhitespace(current)) {
                lexer.next();
                continue;
            }
            var token = lexer.lexChar(current);
            tokens.add(token);
            lexer.next();
        }

        return tokens;
    }
}
