package com.github.mangila.pokedex.shared.https;

import com.github.mangila.pokedex.shared.pokeapi.Header;
import com.github.mangila.pokedex.shared.pokeapi.Request;
import org.junit.jupiter.api.Test;

class RequestTest {

    @Test
    void toRawHttpRequest() {
        var r = new Request("/api/v2/pokemon-species/bulbasaur", new Header[]{
                new Header("key", "value"),
                new Header("accept", "application/json"),
        });
        System.out.println(r.toRawHttpRequest("pokeapi.co"));
    }
}