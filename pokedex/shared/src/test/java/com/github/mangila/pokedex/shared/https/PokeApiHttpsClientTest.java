package com.github.mangila.pokedex.shared.https;

import com.github.mangila.pokedex.shared.pokeapi.Header;
import com.github.mangila.pokedex.shared.pokeapi.PokeApiHttpsClient;
import com.github.mangila.pokedex.shared.pokeapi.Request;
import org.junit.jupiter.api.Test;

class PokeApiHttpsClientTest {

    @Test
    void abc() {
        var http = new PokeApiHttpsClient("pokeapi.co");
        http.connect();
        var k = http.execute(new Request("/api/v2/pokemon-species/bulbasaur", new Header[]{
                new Header("Accept", "application/json"),
        }));
        System.out.println(k);
        http.disconnect();
    }

}