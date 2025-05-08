package com.github.mangila.pokedex.shared.https.internal.json;

import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.json.JsonTokenizer;
import org.junit.jupiter.api.Test;

class JsonTokenizerTest {

    @Test
    void tokenizeFrom() {
        var tokens = JsonTokenizer.tokenizeFrom("""
                {
                "number" : eee234e-5,
                "n" : -1234,
                "b" : {
                    "c" : "asd"
                },
                "tr": true,
                "fa": false,
                "nu": null
                }
                """);
       var tree =  JsonParser.parseTree(tokens);
        System.out.println(tree);
    }
}