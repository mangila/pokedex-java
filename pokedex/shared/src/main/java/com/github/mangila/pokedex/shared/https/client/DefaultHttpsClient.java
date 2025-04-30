package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.func.VoidFunction;
import com.github.mangila.pokedex.shared.https.internal.ResponseHandler;
import com.github.mangila.pokedex.shared.https.internal.ResponseTtlCache;
import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.Response;
import com.github.mangila.pokedex.shared.https.tls.TlsConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;

class DefaultHttpsClient implements HttpsClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultHttpsClient.class);
    private static final ResponseTtlCache TTL_CACHE = new ResponseTtlCache();

    private final String host;
    private final int port;
    private final TlsConnection tlsConnection;

    public DefaultHttpsClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.tlsConnection = new TlsConnection(host, port);
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

    @Override
    public Function<GetRequest, Response> get() {
        return getRequest -> {
            try {
                var rawHttp = getRequest.toHttp(host, tlsConnection.getHttpVersion());
                log.debug("{}", rawHttp);
                tlsConnection.getOutputStream().write(rawHttp.getBytes());
                tlsConnection.getOutputStream().flush();
                var input = tlsConnection.getInputStream();
                var status = ResponseHandler.readStatusLine(input);
                var headers = ResponseHandler.readHeaders(input);
                var body = ResponseHandler.readGzipBody(input, 5041);
                System.out.println(status.length);
                System.out.println(headers.length);
                System.out.println(new String(status));
                System.out.println(new String(headers));
                System.out.println(new String(body));
                return new Response("", Map.of(), "");
            } catch (Exception e) {
                log.error("ERR", e);
                tlsConnection.disconnect();
                throw new RuntimeException(e);
            }
        };
    }
}
