package com.github.mangila.pokedex.shared.pokeapi;

import org.junit.jupiter.api.Test;

class GetRequestTest {

    @Test
    void toHttp() {
        var r = new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{
                new Header("key", "value"),
                new Header("accept", "application/json"),
        });
        System.out.println(r.toHttp("pokeapi.co",""));
    }
}