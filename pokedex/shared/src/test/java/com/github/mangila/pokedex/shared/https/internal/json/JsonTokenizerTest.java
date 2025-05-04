package com.github.mangila.pokedex.shared.https.internal.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonTokenizerTest {

    @Test
    void tokenizeFrom() {
        var tokenizer = new JsonTokenizer();
        var tokens = tokenizer.tokenizeFrom("""
                {
                
                
                "name": "Bulbasaur", \t
                "weight": -6.97.,
                "success" : "escape\\"me\\""
                "esacpe": " asdf\r "    ,
                "true": true,
                "false": false,
                "null": null,
                }
                """);
        System.out.println(tokens);
    }
}