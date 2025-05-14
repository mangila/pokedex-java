package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.cache.ResponseTtlCache;
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
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

public class PokeApiClient {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);
    private static final int END_OF_STREAM = -1;

    private final PokeApiHost host;
    private final TlsConnectionPool pool;
    private final ResponseTtlCache cache;
    private final JsonParser jsonParser;

    public PokeApiClient(PokeApiHost host) {
        this.host = host;
        this.pool = new TlsConnectionPool(host.hostName(), host.port());
        pool.init();
        this.cache = new ResponseTtlCache();
        cache.startEvictionThread();
        this.jsonParser = new JsonParser();
    }

    public Function<JsonRequest, Future<Optional<JsonResponse>>> getJsonAsync() {
        return jsonRequest -> VirtualThreadConfig.newSingleThreadExecutor()
                .submit(() -> this.getJson()
                        .apply(jsonRequest));
    }

    public Function<JsonRequest, Optional<JsonResponse>> getJson() {
        return jsonRequest -> {
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
                var http = jsonRequest.toHttp(host.hostName(), "HTTP/1.1");
                log.debug("{}", http);
                connection.getOutputStream().write(http.getBytes());
                connection.getOutputStream().flush();
                var inputStream = connection.getInputStream();
                var httpStatus = readStatusLine(inputStream);
                var headers = readHeaders(inputStream);
                var body = readGzipJsonBody(inputStream, headers, jsonParser);
                var response = JsonResponse.builder()
                        .httpStatus(httpStatus)
                        .headers(headers)
                        .body(body)
                        .build();
                pool.returnConnection(connection);
                cache.put(path, response);
                return Optional.of(response);
            } catch (Exception e) {
                log.error("ERR", e);
                pool.addNewConnection();
            }
            return Optional.empty();
        };
    }

    private static HttpStatus readStatusLine(InputStream inputStream) {
        try {
            var buffer = new ByteArrayOutputStream();
            int previous = -1;
            while (true) {
                int current = inputStream.read();
                if (current == END_OF_STREAM) {
                    throw new IOException("Stream ended unexpectedly");
                }
                buffer.write(current);
                if (isCrLf(previous, current) && buffer.size() >= 8) {
                    break;
                }
                previous = current;
            }
            var bufferString = buffer.toString(Charset.defaultCharset()).trim();
            log.debug("Status line: {}", bufferString);
            return HttpStatus.fromString(bufferString);
        } catch (Exception e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    private static Headers readHeaders(InputStream inputStream) {
        try {
            var buffer = new ByteArrayOutputStream(2 * 1024);
            var headers = new Headers();
            int previous = -1;
            while (true) {
                int current = inputStream.read();
                if (current == END_OF_STREAM) {
                    throw new IOException("Stream ended unexpectedly");
                }
                buffer.write(current);
                if (isCrLf(previous, current)) {
                    var header = buffer.toString(Charset.defaultCharset()).trim();
                    if (header.isBlank()) {
                        break;
                    }
                    var parts = header.split(": ");
                    if (parts.length == 2) {
                        headers.put(parts[0], parts[1]);
                    }
                    buffer.reset();
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
                                             Headers headers,
                                             JsonParser jsonParser) {
        try {
            if (headers.isGzip() && headers.isJson()) {
                var writeBuffer = new ByteArrayOutputStream(2 * 1024);
                var readBuffer = new byte[2 * 1024];
                if (headers.isChunked()) {
                    log.debug("Chunked GZIP encoding detected");
                    var buffer = new ByteArrayOutputStream(2 * 1024);
                    int previous = -1;
                    while (true) {
                        int current = inputStream.read();
                        if (current == END_OF_STREAM) {
                            break;
                        }
                        buffer.write(current);
                        if (isCrLf(previous, current)) {
                            var chunkLine = buffer.toString(StandardCharsets.US_ASCII).trim();
                            if (chunkLine.equals("0") || chunkLine.isBlank()) {
                                buffer.reset();
                            } else if (chunkLine.matches("[0-9a-fA-F]+")) {
                                int chunkSize = Integer.parseInt(chunkLine, 16);
                                writeBuffer.write(inputStream.readNBytes(chunkSize));
                                buffer.reset();
                            }
                        }
                        previous = current;
                    }
                }
                GZIPInputStream gzip;
                if (writeBuffer.size() == 0) {
                    gzip = new GZIPInputStream(inputStream);
                } else {
                    var allChunks = new ByteArrayInputStream(writeBuffer.toByteArray());
                    writeBuffer.reset();
                    gzip = new GZIPInputStream(allChunks);
                }
                int byteCount;
                while ((byteCount = gzip.read(readBuffer)) != END_OF_STREAM) {
                    writeBuffer.write(readBuffer, 0, byteCount);
                }

                return jsonParser.parseTree(writeBuffer.toByteArray());
            } else {
                throw new IOException("Only gzipped json content encoding is supported");
            }
        } catch (Exception e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * CR (Carriage Return): ASCII value 13 (\r)
     * LF (Line Feed): ASCII value 10 (\n)
     */
    private static boolean isCrLf(int carriageReturn, int lineFeed) {
        return carriageReturn == '\r' && lineFeed == '\n';
    }

}
