package com.github.mangila.pokedex.shared.https.client.json;

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

import java.util.concurrent.CompletableFuture;

public class JsonClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonClient.class);
    private final String host;
    private final TlsConnectionPool pool;
    private final HttpStatusReader statusReader;
    private final HttpHeaderReader headerReader;
    private final HttpBodyReader httpBodyReader;
    private final JsonParser jsonParser;

    public JsonClient(JsonClientConfig config) {
        this.host = config.host();
        this.jsonParser = config.jsonParser();
        this.pool = new TlsConnectionPool(config.poolConfig());
        this.statusReader = new HttpStatusReader();
        this.headerReader = new HttpHeaderReader();
        this.httpBodyReader = new HttpBodyReader();
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
        try {
            Ensure.notNull(request, GetRequest.class);
            TlsConnectionHandler tlsConnectionHandler = pool.borrow();
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
            return JsonResponse.from(status, responseHeaders, body, jsonParser);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pool.offerNewConnection();
            return null;
        } catch (Exception e) {
            LOGGER.error("ERR", e);
            pool.offerNewConnection();
            return null;
        }
    }
}
