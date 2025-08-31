package com.github.mangila.pokedex.api.client.pokeapi;

import com.github.mangila.pokedex.api.client.pokeapi.response.PokeApiClientException;
import com.github.mangila.pokedex.shared.cache.ttl.TtlCache;
import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.https.http.json.JsonClient;
import com.github.mangila.pokedex.shared.https.http.json.JsonClientConfig;
import com.github.mangila.pokedex.shared.https.http.json.JsonResponse;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.https.tls.TlsConnectionPoolConfig;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static com.github.mangila.pokedex.shared.Config.*;

public class PokeApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(PokeApiClient.class);
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
        PokeApiClientConfig config = new PokeApiClientConfig(
                new JsonClientConfig(
                        POKEAPI_HOST,
                        JsonParser.DEFAULT,
                        new TlsConnectionPoolConfig(POKEAPI_HOST, QueueService.getInstance()
                                .getBlockingQueue(TLS_CONNECTION_POOL_QUEUE), POKEAPI_PORT)
                ), TtlCacheConfig.defaultConfig());
        configure(config);
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
    private final TtlCache<PokeApiUri, JsonRoot> ttlCache;

    private PokeApiClient(PokeApiClientConfig config) {
        this.jsonClient = new JsonClient(config.jsonClientConfig());
        this.ttlCache = new TtlCache<>(config.cacheConfig());
    }

    public JsonResponse ensureSuccess(JsonResponse response, Throwable throwable) {
        if (throwable != null) {
            throw new PokeApiClientException("Request failed", throwable);
        }
        if (response == null) {
            throw new PokeApiClientException("Response is null");
        }
        if (!response.isSuccess()) {
            throw new PokeApiClientException("%s".formatted(response.status()), response);
        }
        return response;
    }

    public CompletableFuture<JsonRoot> fetchAsync(PokeApiUri uri) {
        if (ttlCache.hasKey(uri)) {
            return CompletableFuture.completedFuture(ttlCache.get(uri));
        }
        return jsonClient.fetchAsync(uri.toGetRequest())
                .handle(this::ensureSuccess)
                .thenApply(jsonResponse -> {
                    JsonRoot jsonRoot = jsonResponse.body();
                    ttlCache.put(uri, jsonRoot);
                    return jsonRoot;
                });
    }

    public void shutdown() {
        jsonClient.shutdown();
        ttlCache.shutdown();
    }
}
