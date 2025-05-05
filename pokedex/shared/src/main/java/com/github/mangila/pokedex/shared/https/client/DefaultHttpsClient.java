package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.func.VoidFunction;
import com.github.mangila.pokedex.shared.https.internal.ResponseHandler;
import com.github.mangila.pokedex.shared.https.internal.ResponseTtlCache;
import com.github.mangila.pokedex.shared.https.internal.json.JsonParser;
import com.github.mangila.pokedex.shared.https.internal.json.JsonTokenizer;
import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.Response;
import com.github.mangila.pokedex.shared.https.tls.TlsConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

class DefaultHttpsClient implements HttpsClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultHttpsClient.class);
    private static final JsonTokenizer JSON_TOKENIZER = new JsonTokenizer();
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final ResponseTtlCache TTL_CACHE = new ResponseTtlCache(Duration.ofMinutes(5));

    private final String host;
    private final int port;
    private final TlsConnection tlsConnection;

    public DefaultHttpsClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.tlsConnection = new TlsConnection(host, port);
    }

    @Override
    public Function<GetRequest, Response> get() {
        return getRequest -> {
            try {
                var path = getRequest.path();
                if (TTL_CACHE.hasKey(path)) {
                    return TTL_CACHE.get(path);
                }
                var rawHttp = getRequest.toHttp(host, tlsConnection.getHttpVersion());
                log.debug("{}", rawHttp);
                tlsConnection.getOutputStream().write(rawHttp.getBytes());
                tlsConnection.getOutputStream().flush();
                var input = tlsConnection.getInputStream();
                var statusLine = ResponseHandler.readStatusLine(input);
                log.debug("{}", statusLine);
                var headers = ResponseHandler.readHeaders(input);
                var encoding = headers.get("Content-Encoding");
                var contentLength = headers.get("Content-Length");
                var contentType = headers.get("Content-Type");
                if (Objects.equals("gzip", encoding)
                        && Objects.equals("application/json; charset=utf-8", contentType)
                        && Objects.nonNull(contentLength)) {
                    var body = ResponseHandler.readGzipBody(input, Integer.parseInt(contentLength));
                    var tokens = JSON_TOKENIZER.tokenizeFrom(body);
                    var parsed = JSON_PARSER.parse(tokens);
                    var response = new Response(statusLine, headers, parsed.toString());
                    TTL_CACHE.put(path, response);
                    return response;
                }
                return null;
            } catch (Exception e) {
                log.error("ERR", e);
                tlsConnection.disconnect();
                return null;
            }
        };
    }

    @Override
    public Supplier<Boolean> isConnected() {
        return tlsConnection::isConnected;
    }

    @Override
    public VoidFunction disconnect() {
        return () -> {
            try {
                tlsConnection.disconnect();
            } catch (Exception e) {
                log.error("ERR", e);
            }
        };
    }

    @Override
    public VoidFunction connect() {
        return () -> {
            try {
                tlsConnection.connect();
            } catch (Exception e) {
                log.error("ERR", e);
            }
        };
    }
}
