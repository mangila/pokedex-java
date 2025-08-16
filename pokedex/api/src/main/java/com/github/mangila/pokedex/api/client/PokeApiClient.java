package com.github.mangila.pokedex.api.client;

import com.github.mangila.pokedex.api.client.response.EvolutionChainResponse;
import com.github.mangila.pokedex.api.client.response.PokemonsResponse;
import com.github.mangila.pokedex.api.client.response.SpeciesResponse;
import com.github.mangila.pokedex.api.client.response.VarietyResponse;
import com.github.mangila.pokedex.shared.https.client.json.JsonClient;
import com.github.mangila.pokedex.shared.https.client.json.JsonClientUtil;
import com.github.mangila.pokedex.shared.https.client.json.JsonResponse;

import java.util.concurrent.CompletableFuture;

public class PokeApiClient {
    private final JsonClient jsonClient;

    public PokeApiClient(PokeApiClientConfig config) {
        this.jsonClient = new JsonClient(config.jsonClientConfig());
    }

    public CompletableFuture<SpeciesResponse> fetchPokemonSpecies(PokeApiUri uri) {
        return jsonClient.fetchAsync(uri.toGetRequest())
                .thenApply(JsonClientUtil.ensureSuccess())
                .thenApply(JsonResponse::body)
                .thenApply(SpeciesResponse::from);
    }

    public CompletableFuture<VarietyResponse> fetchPokemonVariety(PokeApiUri uri) {
        return jsonClient.fetchAsync(uri.toGetRequest())
                .thenApply(JsonClientUtil.ensureSuccess())
                .thenApply(JsonResponse::body)
                .thenApply(VarietyResponse::from);
    }

    public CompletableFuture<EvolutionChainResponse> fetchEvolutionChain(PokeApiUri uri) {
        return jsonClient.fetchAsync(uri.toGetRequest())
                .thenApply(JsonClientUtil.ensureSuccess())
                .thenApply(JsonResponse::body)
                .thenApply(EvolutionChainResponse::from);
    }

    public CompletableFuture<PokemonsResponse> fetchAllPokemons(int limit) {
        PokeApiUri uri = PokeApiUri.fromString("https://pokeapi.co/api/v2/pokemon-species?limit=" + limit);
        return jsonClient.fetchAsync(uri.toGetRequest())
                .thenApply(JsonClientUtil.ensureSuccess())
                .thenApply(JsonResponse::body)
                .thenApply(PokemonsResponse::from);
    }
}
