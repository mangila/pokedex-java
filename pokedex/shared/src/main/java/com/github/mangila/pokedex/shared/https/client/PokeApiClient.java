package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.cache.ResponseTtlCache;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.func.TriFunction;
import com.github.mangila.pokedex.shared.https.model.*;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.tls.TlsConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
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
                var response = readResponse()
                        .apply(connection.getInputStream());
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

    private Function<InputStream, JsonResponse> readResponse() {
        return inputStream -> readStatusLine()
                .andThen(builder -> readHeaders().apply(inputStream, builder))
                .andThen(builder -> readGzipJsonBody().apply(inputStream, builder.headers(), builder))
                .apply(inputStream, JsonResponse.builder())
                .build();
    }

    private BiFunction<InputStream, JsonResponse.Builder, JsonResponse.Builder> readStatusLine() {
        return (inputStream, builder) -> {
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
                return builder.httpStatus(HttpStatus.fromString(bufferString));
            } catch (Exception e) {
                log.error("ERR", e);
                throw new RuntimeException(e);
            }
        };
    }

    private BiFunction<InputStream, JsonResponse.Builder, JsonResponse.Builder> readHeaders() {
        return (inputStream, builder) -> {
            try {
                var buffer = new ByteArrayOutputStream(8 * 1024);
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
                return builder.headers(headers);
            } catch (Exception e) {
                log.error("ERR", e);
                throw new RuntimeException(e);
            }
        };
    }

    private TriFunction<InputStream, Headers, JsonResponse.Builder, JsonResponse.Builder> readGzipJsonBody() {
        return (inputStream, headers, builder) -> {
            try {
                if (headers.isGzip() && headers.isJson()) {
                    var writeBuffer = new ByteArrayOutputStream(8 * 1024);
                    var readBuffer = ByteBuffer.allocate(8 * 1024);
                    var magicNumberReader = new MagicNumberReader(inputStream);
                    inputStream = magicNumberReader.getInputStream();
                    if (headers.isChunked()) {
                        log.info(magicNumberReader.readFormat());
                        // TODO read chunked gzip response body, some CDN/cache hits from the PokeAPI returns a chunked gzip response
                        throw new UnsupportedOperationException("Not yet implemented");
                    } else {
                        var format = magicNumberReader.readFormat();
                        if (format.equals(MagicNumberReader.GZIP)) {
                            var gzip = new GZIPInputStream(inputStream);
                            int byteCount;
                            while ((byteCount = gzip.read(readBuffer.array())) != END_OF_STREAM) {
                                readBuffer.position(0);
                                writeBuffer.write(readBuffer.array(), 0, byteCount);
                                readBuffer.clear();
                            }
                            return builder.body(jsonParser.parseTree(writeBuffer.toByteArray()));
                        } else {
                            throw new IOException("Did not find gzip header: " + format);
                        }
                    }
                } else {
                    throw new IOException("Only gzipped json content encoding is supported");
                }
            } catch (Exception e) {
                log.error("ERR", e);
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * CR (Carriage Return): ASCII value 13 (\r)
     * LF (Line Feed): ASCII value 10 (\n)
     */
    private static boolean isCrLf(int carriageReturn, int lineFeed) {
        return carriageReturn == '\r' && lineFeed == '\n';
    }

}
