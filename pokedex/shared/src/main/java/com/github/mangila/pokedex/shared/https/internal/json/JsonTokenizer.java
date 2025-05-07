package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class JsonTokenizer {

    public static Queue<JsonToken> tokenizeFrom(byte[] data) {
        return tokenize(data);
    }

    public static Queue<JsonToken> tokenizeFrom(String data) {
        return tokenize(data.getBytes());
    }

    private static Queue<JsonToken> tokenize(byte[] data) {
        Objects.requireNonNull(data, "json data must not be null");
        var lexer = new JsonLexer(data);
        var tokens = new LinkedList<JsonToken>();
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
