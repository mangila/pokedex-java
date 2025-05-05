package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonTokenizer {

    public List<JsonToken> tokenizeFrom(byte[] data) {
        return tokenize(data);
    }

    public List<JsonToken> tokenizeFrom(String data) {
        return tokenize(data.getBytes());
    }

    private List<JsonToken> tokenize(byte[] data) {
        Objects.requireNonNull(data, "json data must not be null");
        var lexer = new JsonLexer(data);
        var tokens = new ArrayList<JsonToken>();
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
