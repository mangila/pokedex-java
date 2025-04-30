package com.github.mangila.pokedex.shared.pokeapi;

import org.junit.jupiter.api.Test;

class PokeApiClientTest {

    @Test
    void abc() {
        var https = new PokeApiClient(new PokeApiHost("pokeapi.co"), Boolean.TRUE);
        https.get(new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{
                new Header("accept-encoding", "gzip"),
        }));

        https.get(new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{
                new Header("accept-encoding", "gzip"),
        }));
    }

}