package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import org.junit.jupiter.api.Test;

import java.util.List;

class PokeApiClientTest {

    @Test
    void getJson() {
        var client = new PokeApiClient(new PokeApiHost("pokeapi.co", 443));
        var optional = client.getJson(3)
                .apply(new JsonRequest("GET", "/api/v2/pokemon-species/bulbasaur", List.of()));
        assert optional.isPresent();
        System.out.println(optional.get());
    }
}