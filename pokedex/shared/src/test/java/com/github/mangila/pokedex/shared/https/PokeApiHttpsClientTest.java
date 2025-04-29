package com.github.mangila.pokedex.shared.https;

import com.github.mangila.pokedex.shared.pokeapi.Header;
import com.github.mangila.pokedex.shared.pokeapi.PokeApiHttpsClient;
import com.github.mangila.pokedex.shared.pokeapi.GetRequest;
import com.github.mangila.pokedex.shared.pokeapi.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PokeApiHttpsClientTest {

    @Test
    void abc() throws IOException {
        try (var http = new PokeApiHttpsClient("pokeapi.co")) {
            var body = http.get.andThen(Response::body)
                    .apply(new GetRequest("/api/v2/pokemon-species/bulbasaur", new Header[]{
                            new Header("accept-encoding", "gzip"),
                    }));
            http.get.andThen(Response::body)
                    .apply(new GetRequest("/api/v2/pokemon-species/charmander", new Header[]{}));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}