package com.github.mangila.pokedex.shared.pokeapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PokeApiMediaClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PokeApiMediaClient.class);

    private final HttpsClient https;

    public PokeApiMediaClient(String host) {
        this.https = new HttpsClient(host);
        this.https.connect();
    }

    @Override
    public void close() throws Exception {
        this.https.disconnect();
    }

}
