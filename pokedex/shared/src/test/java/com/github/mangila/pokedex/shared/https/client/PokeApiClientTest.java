package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.Header;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import org.junit.jupiter.api.Test;

class PokeApiClientTest {

    @Test
    void abc() {
        var https = new PokeApiClient(new PokeApiHost("pokeapi.co", 443));
        https.connect();
        var s = https.get().apply(new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{}));
        System.out.println(s);
        https.disconnect();
    }

}