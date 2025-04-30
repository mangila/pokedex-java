package com.github.mangila.pokedex.shared.pokeapi;

import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.Header;
import com.github.mangila.pokedex.shared.https.model.Response;
import org.junit.jupiter.api.Test;

class PokeApiHttpsClientTest {

    @Test
    void abc() {
        var https = new PokeApiClient(new PokeApiHost("pokeapi.co"));
        https.connect();
        https.get()
                .andThen(Response::body)
                .apply(new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{}));

        https.get()
                .apply(new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{
                }));
        https.disconnect();
    }

}