package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.cache.ttl.TtlCache;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.model.Headers;
import com.github.mangila.pokedex.shared.https.model.HttpStatus;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.json.model.JsonTree;
import com.github.mangila.pokedex.shared.model.primitives.PokeApiHost;
import com.github.mangila.pokedex.shared.tls.pool.TlsConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static com.github.mangila.pokedex.shared.https.client.PokeApiClientUtil.isCrLf;

public class PokeApiClient {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);
    private static final int END_OF_STREAM = -1;
    private static final Pattern HEX_DECIMAL = Pattern.compile("^[0-9a-fA-F]+$");

    private final PokeApiHost host;
    private final TlsConnectionPool pool;
    private final TtlCache<String, JsonResponse> cache;

    public PokeApiClient(PokeApiClientConfig config) {
        this.host = config.pokeApiHost();
        this.pool = new TlsConnectionPool(config.tlsConnectionPoolConfig());
        pool.init();
        this.cache = new TtlCache<>(config.ttlCacheConfig());
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
            var connection = pool.borrow();
            var http = jsonRequest.toHttp(host.host(), "HTTP/1.1");
            log.debug("{}", http);
            connection.getOutputStream().write(http.getBytes());
            connection.getOutputStream().flush();
            var inputStream = connection.getInputStream();
            var httpStatus = readStatusLine(inputStream);
            var headers = readHeaders(inputStream);
            var body = readGzipJsonBody(inputStream, headers);
            var response = new JsonResponse(httpStatus, headers, body);
            pool.offer(connection);
            cache.put(path, response);
            return Optional.of(response);
        } catch (Exception e) {
            log.error("ERR", e);
            var connection = pool.createNewConnection();
            pool.offer(connection);
        }
        return Optional.empty();
    }

    /**
     * Uses Two Pointers Algorithm technique - tracking current and previous characters
     * from the same end to detect delimiters in the stream. Reads the HTTP status-line
     */
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

    /**
     * Uses the Two Pointers Algorithm technique - tracking current and previous characters from the same end
     * to detect delimiters in the input stream and create an HTTP Headers object.
     */
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

    /**
     * Only accepts gzipped JSON -- Java GzipInputStream decompress the bytes and returns parsed JSON
     */
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

    /**
     * <summary>
     * The Two Pointers Algorithm technique is used here to parse HTTP protocol data from input stream.
     * By comparing current and previous bytes we detect required delimiters (CRLF).<br>
     * Reads HTTP 1.1 chunked transfer -- reads until '0' is found and skip the rest of the bytes in socket buffer<br>
     * Fills chunkBuffer with every chunk and returns as a byte array
     * </summary>
     */
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

}
