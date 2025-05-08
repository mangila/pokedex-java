package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.func.VoidFunction;
import com.github.mangila.pokedex.shared.cache.ResponseTtlCache;
import com.github.mangila.pokedex.shared.json.JsonResponseReader;
import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.Response;
import com.github.mangila.pokedex.shared.tls.TlsConnection;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.json.JsonTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

class DefaultHttpsClient implements HttpsClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultHttpsClient.class);
    private static final ResponseTtlCache TTL_CACHE = new ResponseTtlCache();

    private final String host;
    private final int port;
    private final TlsConnection tlsConnection;
    private final JsonResponseReader jsonResponseReader;

    public DefaultHttpsClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.tlsConnection = new TlsConnection(host, port);
        this.jsonResponseReader = new JsonResponseReader(tlsConnection);
    }

    @Override
    public Function<GetRequest, Response> getJson() {
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
                var statusLine = JsonResponseReader.readStatusLine(input);
                log.debug("{}", statusLine);
                var headers = JsonResponseReader.readHeaders(input);
                var encoding = headers.get("Content-Encoding");
                var contentType = headers.get("Content-Type");
                if (Objects.equals("gzip", encoding)
                        && Objects.equals("application/json; charset=utf-8", contentType)) {
                    var body = JsonResponseReader.readGzipBody(input);
                    var tokens = JsonTokenizer.tokenizeFrom(body);
                    var parsed = JsonParser.parseTree(tokens);
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
