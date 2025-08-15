package com.github.mangila.pokedex.shared.https.client.json;

import com.github.mangila.pokedex.shared.cache.ttl.TtlCache;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.HttpBodyReader;
import com.github.mangila.pokedex.shared.https.client.HttpHeaderReader;
import com.github.mangila.pokedex.shared.https.client.HttpStatusReader;
import com.github.mangila.pokedex.shared.https.model.Body;
import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.ResponseHeaders;
import com.github.mangila.pokedex.shared.https.model.Status;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.tls.TlsConnectionHandler;
import com.github.mangila.pokedex.shared.tls.pool.TlsConnectionPool;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class JsonClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonClient.class);
    private final String host;
    private final TlsConnectionPool pool;
    private final TtlCache<String, JsonResponse> responseTtlCache;
    private final HttpStatusReader statusReader;
    private final HttpHeaderReader headerReader;
    private final HttpBodyReader httpBodyReader;
    private final JsonParser jsonParser;

    public JsonClient(String host,
                      JsonParser jsonParser,
                      TlsConnectionPool pool,
                      TtlCache<String, JsonResponse> responseTtlCache) {
        this.host = host;
        this.jsonParser = jsonParser;
        this.pool = pool;
        pool.init();
        this.responseTtlCache = responseTtlCache;
        this.statusReader = new HttpStatusReader();
        this.headerReader = new HttpHeaderReader();
        this.httpBodyReader = new HttpBodyReader();
    }

    public CompletableFuture<JsonResponse> fetchAsync(GetRequest request) {
        return CompletableFuture.supplyAsync(() -> this.fetch(request), VirtualThreadConfig.newSingleThreadExecutor());
    }

    public @Nullable JsonResponse fetch(GetRequest request) {
        try {
            Ensure.notNull(request, GetRequest.class);
            String path = request.path();
            if (responseTtlCache.hasKey(path)) {
                return responseTtlCache.get(path);
            }
            TlsConnectionHandler tlsConnectionHandler = pool.borrow(Duration.ofSeconds(30));
            String rawHttpRequest = request.toRawHttp(host);
            LOGGER.debug("{}", rawHttpRequest);
            tlsConnectionHandler.writeAndFlush(rawHttpRequest.getBytes());
            Status status = statusReader.read(tlsConnectionHandler);
            ResponseHeaders responseHeaders = headerReader.read(tlsConnectionHandler);
            if (!responseHeaders.isJson()) {
                throw new IllegalStateException("Response is not JSON");
            }
            LOGGER.debug("Response is JSON");
            Body body = httpBodyReader.read(responseHeaders, tlsConnectionHandler);
            JsonResponse response = JsonResponse.from(status, responseHeaders, body, jsonParser);
            pool.offer(tlsConnectionHandler);
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
