package com.github.mangila.pokedex.shared.pokeapi;

import com.github.mangila.pokedex.shared.func.VoidFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public final class PokeApiClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);

    private final HttpsClient https;

    public PokeApiClient(PokeApiHost pokeApiHost,
                         boolean autoConnect) {
        this.https = new HttpsClient(pokeApiHost.hostName());
        if (autoConnect) {
            this.connect().apply();
        }
    }

    public Function<GetRequest, Response> get() {
        return this.https.GET;
    }

    public VoidFunction connect() {
        return this.https.CONNECT;
    }

    @Override
    public void close() throws Exception {
        this.https.DISCONNECT.apply();
    }
}
