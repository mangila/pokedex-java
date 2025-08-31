package com.github.mangila.pokedex.shared.https.http.json;

import com.github.mangila.pokedex.shared.https.http.HttpBodyReader;
import com.github.mangila.pokedex.shared.https.http.HttpHeaderReader;
import com.github.mangila.pokedex.shared.https.http.HttpStatusReader;
import com.github.mangila.pokedex.shared.https.model.Body;
import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.ResponseHeaders;
import com.github.mangila.pokedex.shared.https.model.Status;
import com.github.mangila.pokedex.shared.https.tls.TlsConnectionHandle;
import com.github.mangila.pokedex.shared.https.tls.TlsConnectionPool;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.util.Ensure;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class JsonClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonClient.class);
    private final String host;
    private final TlsConnectionPool pool;
    private final JsonParser jsonParser;

    public JsonClient(JsonClientConfig config) {
        this.host = config.host();
        this.jsonParser = config.jsonParser();
        this.pool = new TlsConnectionPool(config.poolConfig());
    }

    public void shutdown() {
        pool.close();
    }

    public CompletableFuture<@Nullable JsonResponse> fetchAsync(GetRequest request) {
        return CompletableFuture.supplyAsync(() -> this.fetch(request), VirtualThreadFactory.THREAD_PER_TASK_EXECUTOR);
    }

    /**
     * This is the hot code path for the JsonClient when doing GET requests to a remote service.
     * Content-Type: application/json header must be present in the response.
     */
    public @Nullable JsonResponse fetch(GetRequest request) {
        Ensure.notNull(request, GetRequest.class);
        try {
            TlsConnectionHandle tlsConnectionHandle = pool.borrow();
            String rawHttpRequest = request.toRawHttp(host);
            LOGGER.debug("{}", rawHttpRequest);
            tlsConnectionHandle.writeAndFlush(rawHttpRequest.getBytes(StandardCharsets.UTF_8));
            Status status = HttpStatusReader.read(tlsConnectionHandle);
            ResponseHeaders responseHeaders = HttpHeaderReader.read(tlsConnectionHandle);
            if (!responseHeaders.isJson()) {
                throw new IllegalStateException("Response is not JSON");
            }
            Body body = HttpBodyReader.read(responseHeaders, tlsConnectionHandle);
            pool.offer(tlsConnectionHandle);
            return JsonResponse.from(status, responseHeaders, body, jsonParser);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for a connection");
            Thread.currentThread().interrupt();
            pool.offerNewConnection();
            return null;
        } catch (Exception e) {
            LOGGER.error("Failed to request JSON {}", request, e);
            pool.offerNewConnection();
            return null;
        }
    }
}
