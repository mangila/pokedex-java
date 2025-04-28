package com.github.mangila.pokedex.shared.https;

import com.github.mangila.pokedex.shared.pokeapi.Header;
import com.github.mangila.pokedex.shared.pokeapi.PokeApiHttpClient;
import com.github.mangila.pokedex.shared.pokeapi.Request;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class PokeApiHttpClientTest {

    @Test
    void abc() {
        var http = new PokeApiHttpClient();
        http.connect();
        var k = http.execute(new Request("/api/v2/pokemon-species/bulbasaur", new Header[]{
                new Header("Accept", "application/json"),
        }));
        System.out.println(Arrays.toString(k));
        http.disconnect();
    }

}