package com.github.mangila.pokedex.shared.pokeapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PokeApiClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);

    private final HttpsClient https;

    public PokeApiClient(PokeApiHost pokeApiHost,
                         boolean autoConnect) {
        this.https = new HttpsClient(pokeApiHost.hostName());
        if (autoConnect) {
            this.connect();
        }
    }

    public Response get(GetRequest getRequest) {
        return this.https.get(getRequest);
    }

    public void connect() {
        this.https.connect();
    }

    @Override
    public void close() throws Exception {
        this.https.disconnect();
    }
}
