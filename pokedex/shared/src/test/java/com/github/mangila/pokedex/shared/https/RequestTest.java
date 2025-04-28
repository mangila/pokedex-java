package com.github.mangila.pokedex.shared.https;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestTest {

    @Test
    void toRawHttpRequest() {
        var r = new Request(URI.create("https://pokeapi.co/api/v2/pokemon-species/bulbasaur"), "GET", new Header[]{
                new Header("key", "value"),
                new Header("accept", "application/json"),
        });
        System.out.println(r.toRawHttpRequest());
    }
}