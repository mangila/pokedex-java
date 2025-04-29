package com.github.mangila.pokedex.shared.pokeapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PokeApiHttpsClient extends HttpsClient {

    private static final Logger log = LoggerFactory.getLogger(PokeApiHttpsClient.class);

    public PokeApiHttpsClient(String host) {
        super(host);
        connect();
    }

    @Override
    Response get(GetRequest getRequest) {
        try {
            var outputStream = getSocket().getOutputStream();
            outputStream.write(getRequest.toHttp(getHost()).getBytes());
            outputStream.flush();
            var statusCode = readStatusCode();
            var headers = readHeaders();
            var body = readGzipBody(headers);
            return new Response(statusCode, body);
        } catch (Exception e) {
            log.error("ERR", e);
            disconnect();
        }
        return new Response("", "");
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }
}
