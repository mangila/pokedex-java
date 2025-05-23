package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.cache.JsonResponseTtlCache;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.model.*;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.json.model.JsonTree;
import com.github.mangila.pokedex.shared.tls.TlsConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class PokeApiClient {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);
    private static final int END_OF_STREAM = -1;
    private static final Pattern HEX_DECIMAL = Pattern.compile("^[0-9a-fA-F]+$");

    private final PokeApiHost host;
    private final TlsConnectionPool pool;
    private final JsonResponseTtlCache cache;

    public PokeApiClient(PokeApiClientConfig config) {
        this.host = config.pokeApiHost();
        this.pool = new TlsConnectionPool(config.tlsConnectionPoolConfig());
        pool.init();
        this.cache = new JsonResponseTtlCache(config.jsonResponseTtlCacheConfig());
        cache.startEvictionThread();
    }

    public CompletableFuture<Optional<JsonResponse>> getJsonAsync(JsonRequest jsonRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.getJson(jsonRequest);
            } catch (InterruptedException e) {
                log.error("ERR", e);
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }, VirtualThreadConfig.newSingleThreadExecutor());
    }

    public Optional<JsonResponse> getJson(JsonRequest jsonRequest) throws InterruptedException {
        try {
            var path = jsonRequest.path();
            if (cache.hasKey(path)) {
                return Optional.of(cache.get(path));
            }
            var tlsConnectionOptional = pool.borrow(Duration.ofSeconds(30));
            if (tlsConnectionOptional.isEmpty()) {
                log.warn("Connection pool borrow failed");
                return Optional.empty();
            }
            var connection = tlsConnectionOptional.get();
            var http = jsonRequest.toHttp(host.host(), "HTTP/1.1");
            log.debug("{}", http);
            connection.getOutputStream().write(http.getBytes());
            connection.getOutputStream().flush();
            var inputStream = connection.getInputStream();
            var httpStatus = readStatusLine(inputStream);
            var headers = readHeaders(inputStream);
            var body = readGzipJsonBody(inputStream, headers);
            var response = new JsonResponse(httpStatus, headers, body);
            pool.returnConnection(connection);
            cache.put(path, response);
            return Optional.of(response);
        } catch (Exception e) {
            log.error("ERR", e);
            pool.addNewConnection();
        }
        return Optional.empty();
    }

    private static HttpStatus readStatusLine(InputStream inputStream) {
        try {
            var lineBuffer = new ByteArrayOutputStream();
            int previous = -1;
            while (true) {
                int current = inputStream.read();
                if (current == END_OF_STREAM) {
                    throw new IOException("Stream ended unexpectedly");
                }
                lineBuffer.write(current);
                if (isCrLf(previous, current) && lineBuffer.size() > 8) {
                    break;
                }
                previous = current;
            }
            var bufferString = lineBuffer.toString(Charset.defaultCharset()).trim();
            log.debug("Status line: {}", bufferString);
            return HttpStatus.fromString(bufferString);
        } catch (Exception e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    private static Headers readHeaders(InputStream inputStream) {
        try {
            var lineBuffer = new ByteArrayOutputStream(1024);
            var headers = new Headers();
            int current;
            int previous = -1;
            while (true) {
                current = inputStream.read();
                if (current == END_OF_STREAM) {
                    throw new IOException("Stream ended unexpectedly");
                }
                lineBuffer.write(current);
                if (isCrLf(previous, current)) {
                    var header = lineBuffer.toString(Charset.defaultCharset()).trim();
                    if (header.isBlank()) {
                        break;
                    }
                    var parts = header.split(": ", 2);
                    headers.put(parts[0], parts[1]);
                    lineBuffer.reset();
                }
                previous = current;
            }
            return headers;
        } catch (Exception e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    private static JsonTree readGzipJsonBody(InputStream inputStream,
                                             Headers headers) {
        try {
            if (headers.isGzip() && headers.isJson()) {
                if (headers.isChunked()) {
                    log.debug("Chunked GZIP encoding detected");
                    byte[] allChunks = readChunkedGzipJsonBody(inputStream);
                    var decompressed = new GZIPInputStream(new ByteArrayInputStream(allChunks))
                            .readAllBytes();
                    return JsonParser.getInstance().parseTree(decompressed);
                }
                var decompressed = new GZIPInputStream(inputStream)
                        .readAllBytes();
                return JsonParser.getInstance().parseTree(decompressed);
            } else {
                throw new IOException("Only gzipped json content encoding is supported");
            }
        } catch (Exception e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    private static byte[] readChunkedGzipJsonBody(InputStream inputStream) throws IOException {
        var chunkLineBuffer = new ByteArrayOutputStream();
        var chunkBuffer = new ByteArrayOutputStream(1024);
        int previous = -1;
        while (true) {
            int current = inputStream.read();
            if (current == END_OF_STREAM) {
                break;
            }
            chunkLineBuffer.write(current);
            if (isCrLf(previous, current)) {
                var chunkLine = chunkLineBuffer.toString(StandardCharsets.US_ASCII).trim();
                if (chunkLine.equals("0")) {
                    inputStream.skipNBytes(inputStream.available());
                    break;
                } else if (HEX_DECIMAL.matcher(chunkLine).matches()) {
                    int chunkSize = Integer.parseInt(chunkLine, 16);
                    chunkBuffer.write(inputStream.readNBytes(chunkSize));
                }
                chunkLineBuffer.reset();
            }
            previous = current;
        }

        return chunkBuffer.toByteArray();
    }

    /**
     * CR (Carriage Return): ASCII value 13 (\r)
     * LF (Line Feed): ASCII value 10 (\n)
     */
    private static boolean isCrLf(int carriageReturn, int lineFeed) {
        return carriageReturn == '\r' && lineFeed == '\n';
    }

}
