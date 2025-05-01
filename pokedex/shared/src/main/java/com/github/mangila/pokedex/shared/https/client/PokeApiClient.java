package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.https.model.Response;

import java.util.function.Function;

public class PokeApiClient implements AutoCloseable {

    private final HttpsClient https;

    public PokeApiClient(PokeApiHost pokeApiHost) {
        this.https = new DefaultHttpsClient(pokeApiHost.hostName(), pokeApiHost.port());
    }

    public void disconnect() {
        this.https.disconnect()
                .apply();
    }

    public void connect() {
        this.https.connect()
                .apply();
    }

    public Function<GetRequest, Response> get() {
        return this.https.get();
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }
}
