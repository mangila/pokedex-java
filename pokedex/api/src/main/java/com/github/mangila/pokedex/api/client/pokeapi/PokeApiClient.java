package com.github.mangila.pokedex.api.client.pokeapi;

import com.github.mangila.pokedex.api.client.pokeapi.response.EvolutionChainResponse;
import com.github.mangila.pokedex.api.client.pokeapi.response.PokeApiClientException;
import com.github.mangila.pokedex.api.client.pokeapi.response.VarietyResponse;
import com.github.mangila.pokedex.shared.Config;
import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.https.client.json.JsonClient;
import com.github.mangila.pokedex.shared.https.client.json.JsonClientConfig;
import com.github.mangila.pokedex.shared.https.client.json.JsonResponse;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;
import com.github.mangila.pokedex.shared.tls.TlsConnectionPoolConfig;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class PokeApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(PokeApiClient.class);
    private static final PokeApiClientConfig DEFAULT_CONFIG = new PokeApiClientConfig(
            new JsonClientConfig(
                    Config.POKEAPI_HOST,
                    JsonParser.DEFAULT,
                    new TlsConnectionPoolConfig(Config.POKEAPI_HOST, Config.POKEAPI_PORT, 5),
                    TtlCacheConfig.defaultConfig()
            )
    );
    private static PokeApiClientConfig config;

    private static final class Holder {
        private static final PokeApiClient INSTANCE = new PokeApiClient(config);
    }

    public static PokeApiClient getInstance() {
        Ensure.notNull(config, "PokeApiClient must be configured");
        return PokeApiClient.Holder.INSTANCE;
    }

    public static void configureDefaultSettings() {
        LOGGER.info("Configuring PokeApiClient with default config");
        configure(DEFAULT_CONFIG);
    }

    public static void configure(PokeApiClientConfig config) {
        Ensure.notNull(config, "PokeApiClientConfig must not be null");
        if (PokeApiClient.config != null) {
            throw new IllegalStateException("PokeApiClientConfig is already configured");
        }
        LOGGER.info("Configuring PokeApiClient with {}", config);
        PokeApiClient.config = config;
    }

    private final JsonClient jsonClient;

    private PokeApiClient(PokeApiClientConfig config) {
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

    public CompletableFuture<JsonRoot> fetch(PokeApiUri uri) {
        return jsonClient.fetchAsync(uri.toGetRequest())
                .handle(this::ensureSuccess)
                .thenApply(JsonResponse::body);
    }

    public CompletableFuture<JsonRoot> fetchPokemonSpecies(PokeApiUri uri) {
        return jsonClient.fetchAsync(uri.toGetRequest())
                .handle(this::ensureSuccess)
                .thenApply(JsonResponse::body);
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
}
