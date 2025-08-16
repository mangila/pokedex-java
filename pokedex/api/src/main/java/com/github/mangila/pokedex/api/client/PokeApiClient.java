package com.github.mangila.pokedex.api.client;

import com.github.mangila.pokedex.api.client.response.*;
import com.github.mangila.pokedex.shared.https.client.json.JsonClient;
import com.github.mangila.pokedex.shared.https.client.json.JsonResponse;

import java.util.concurrent.CompletableFuture;

public class PokeApiClient {
    private final JsonClient jsonClient;

    public PokeApiClient(PokeApiClientConfig config) {
        this.jsonClient = new JsonClient(config.jsonClientConfig());
    }

    public JsonResponse ensureSuccess(JsonResponse response, Throwable throwable) {
        if (throwable != null) {
            throw new PokeApiClientException("Request failed", throwable);
        }
        if (!response.isSuccess()) {
            throw new PokeApiClientException("%s".formatted(response.status()), response);
        }
        return response;
    }

    public CompletableFuture<SpeciesResponse> fetchPokemonSpecies(PokeApiUri uri) {
        return jsonClient.fetchAsync(uri.toGetRequest())
                .handle(this::ensureSuccess)
                .thenApply(JsonResponse::body)
                .thenApply(SpeciesResponse::from);
    }

    public CompletableFuture<VarietyResponse> fetchPokemonVariety(PokeApiUri uri) {
        return jsonClient.fetchAsync(uri.toGetRequest())
                .handle(this::ensureSuccess)
                .thenApply(JsonResponse::body)
                .thenApply(VarietyResponse::from);
    }

    public CompletableFuture<EvolutionChainResponse> fetchEvolutionChain(PokeApiUri uri) {
        return jsonClient.fetchAsync(uri.toGetRequest())
                .handle(this::ensureSuccess)
                .thenApply(JsonResponse::body)
                .thenApply(EvolutionChainResponse::from);
    }

    public CompletableFuture<PokemonsResponse> fetchAllPokemons(int limit) {
        PokeApiUri uri = PokeApiUri.from("https://pokeapi.co/api/v2/pokemon-species?limit=" + limit);
        return jsonClient.fetchAsync(uri.toGetRequest())
                .handle(this::ensureSuccess)
                .thenApply(JsonResponse::body)
                .thenApply(PokemonsResponse::from);
    }
}
