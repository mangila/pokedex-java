package com.github.mangila.pokedex.shared.pokeapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;

public final class PokeApiClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);

    private final HttpsClient https;

    public PokeApiClient(String host) {
        this.https = new HttpsClient(host);
        this.https.connect();
    }

    public Response get(GetRequest getRequest) {
        return https.get(getRequest);
    }

    @Override
    public void close() throws Exception {
        this.https.disconnect();
    }
}
