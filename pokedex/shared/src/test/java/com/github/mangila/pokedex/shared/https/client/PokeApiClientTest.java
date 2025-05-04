package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.Header;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import org.junit.jupiter.api.Test;

class PokeApiClientTest {

    @Test
    void abc() {
        try (var https = new PokeApiClient(new PokeApiHost("pokeapi.co", 443))
                .connect()) {
            var s = https.get()
                    .apply(new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{}));
            s = https.get()
                    .apply(new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{}));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}