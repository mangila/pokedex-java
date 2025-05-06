package com.github.mangila.pokedex.shared.https.internal.json;

import org.junit.jupiter.api.Test;

class JsonTokenizerTest {

    @Test
    void tokenizeFrom() {
        var tokenizer = new JsonTokenizer();
        var tokens = tokenizer.tokenizeFrom("""
                {
                "n" : -1234,
                "number": {
                    "arrays": [1,2,3]
                },
                }
                """);
        var par = new JsonParser();
        par.parse(tokens);
    }
}