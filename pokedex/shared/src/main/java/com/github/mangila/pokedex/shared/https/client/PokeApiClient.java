package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class PokeApiClient {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);

    private final HttpsClient https;

    public PokeApiClient(PokeApiHost pokeApiHost) {
        this.https = new DefaultHttpsClient(pokeApiHost.hostName(), 443);
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
}
