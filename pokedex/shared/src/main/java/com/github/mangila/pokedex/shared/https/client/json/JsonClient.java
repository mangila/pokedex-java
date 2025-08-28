package com.github.mangila.pokedex.shared.https.client.json;

import com.github.mangila.pokedex.shared.cache.ttl.TtlCache;
import com.github.mangila.pokedex.shared.https.client.HttpBodyReader;
import com.github.mangila.pokedex.shared.https.client.HttpHeaderReader;
import com.github.mangila.pokedex.shared.https.client.HttpStatusReader;
import com.github.mangila.pokedex.shared.https.model.Body;
import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.ResponseHeaders;
import com.github.mangila.pokedex.shared.https.model.Status;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.tls.TlsConnectionHandler;
import com.github.mangila.pokedex.shared.tls.TlsConnectionPool;
import com.github.mangila.pokedex.shared.util.Ensure;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class JsonClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonClient.class);
    private final String host;
    private final TlsConnectionPool pool;
    private final TtlCache<String, JsonResponse> responseTtlCache;
    private final HttpStatusReader statusReader;
    private final HttpHeaderReader headerReader;
    private final HttpBodyReader httpBodyReader;
    private final JsonParser jsonParser;
    private final ExecutorService executor;

    public JsonClient(JsonClientConfig config) {
        this.host = config.host();
        this.jsonParser = config.jsonParser();
        this.pool = new TlsConnectionPool(config.poolConfig());
        this.responseTtlCache = new TtlCache<>(config.ttlCacheConfig());
        this.statusReader = new HttpStatusReader();
        this.headerReader = new HttpHeaderReader();
        this.httpBodyReader = new HttpBodyReader();
        this.executor = VirtualThreadFactory.newFixedThreadPool(256);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            pool.close();
            responseTtlCache.shutdownEvictionThread();
            responseTtlCache.clear();
            VirtualThreadFactory.terminateGracefully(executor);
        }));
    }

    public CompletableFuture<@Nullable JsonResponse> fetchAsync(GetRequest request) {
        return CompletableFuture.supplyAsync(() -> this.fetch(request), executor);
    }

    public @Nullable JsonResponse fetch(GetRequest request) {
        try {
            Ensure.notNull(request, GetRequest.class);
            String path = request.path();
            if (responseTtlCache.hasKey(path)) {
                return responseTtlCache.get(path);
            }
            TlsConnectionHandler tlsConnectionHandler = pool.borrowWithRetry(Duration.ofSeconds(30), 3);
            if (tlsConnectionHandler == null) {
                throw new IllegalStateException("No connection available");
            }
            String rawHttpRequest = request.toRawHttp(host);
            LOGGER.debug("{}", rawHttpRequest);
            tlsConnectionHandler.writeAndFlush(rawHttpRequest.getBytes());
            Status status = statusReader.read(tlsConnectionHandler);
            ResponseHeaders responseHeaders = headerReader.read(tlsConnectionHandler);
            if (!responseHeaders.isJson()) {
                throw new IllegalStateException("Response is not JSON");
            }
            Body body = httpBodyReader.read(responseHeaders, tlsConnectionHandler);
            pool.offer(tlsConnectionHandler);
            JsonResponse response = JsonResponse.from(status, responseHeaders, body, jsonParser);
            if (response.isSuccess()) {
                responseTtlCache.put(path, response);
            }
            return response;
        } catch (Exception e) {
            LOGGER.error("ERR", e);
            pool.offerNewConnection();
            return null;
        }
    }
}
