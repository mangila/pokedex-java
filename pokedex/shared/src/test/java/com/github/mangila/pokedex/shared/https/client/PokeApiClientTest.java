package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;


class PokeApiClientTest {

    private static final PokeApiClient GENERAL_PURPOSE_TEST_CLIENT = new PokeApiClient(PokeApiHost.fromDefault());

    @Test
    void getJson() throws ExecutionException, InterruptedException {
        var threads = Executors.newFixedThreadPool(1, Thread.ofVirtual().factory());

        var t1 = threads.submit(() -> GENERAL_PURPOSE_TEST_CLIENT.getJson().apply(new JsonRequest("GET", "/api/v2/pokemon-species/1", List.of())));
        var t2 = threads.submit(() -> GENERAL_PURPOSE_TEST_CLIENT.getJson().apply(new JsonRequest("GET", "/api/v2/pokemon-species/4", List.of())));
        var t3 = threads.submit(() -> GENERAL_PURPOSE_TEST_CLIENT.getJson().apply(new JsonRequest("GET", "/api/v2/pokemon-species/7", List.of())));

        var response1 = t1.get();
        var response2 = t2.get();
        var response3 = t3.get();

        var bulbasaur = response1.get();
        var charmander = response2.get();
        var squritle = response3.get();

        System.out.println(bulbasaur);

    }
}