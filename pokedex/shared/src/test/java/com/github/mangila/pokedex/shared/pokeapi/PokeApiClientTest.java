package com.github.mangila.pokedex.shared.pokeapi;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class PokeApiClientTest {

    @Test
    void abc() throws IOException {
        var https = new PokeApiClient("pokeapi.co");

        https.get(new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{
                new Header("accept-encoding", "gzip"),
        }));

        https.get(new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{
                new Header("accept-encoding", "gzip"),
        }));
    }

}