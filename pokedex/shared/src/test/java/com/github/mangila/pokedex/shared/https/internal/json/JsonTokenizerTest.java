package com.github.mangila.pokedex.shared.https.internal.json;

import org.junit.jupiter.api.Test;

class JsonTokenizerTest {

    @Test
    void tokenizeFrom() {
        var tokens = JsonTokenizer.tokenizeFrom("""
                {
                "n" : -1234,
                "b" : {
                    "c" : "asd"
                },
                "tr": true,
                "fa": false,
                "nu": null
                }
                """);
        JsonParser.parseTree(tokens);
    }
}