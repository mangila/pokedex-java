package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.GetRequest;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.https.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class PokeApiClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);
    private final HttpsClient https;

    public PokeApiClient(PokeApiHost pokeApiHost) {
        this.https = new DefaultHttpsClient(pokeApiHost.hostName(), pokeApiHost.port());
    }

    public PokeApiClient disconnect() {
        this.https.disconnect()
                .apply();
        return this;
    }

    public PokeApiClient connect() {
        this.https.connect()
                .apply();
        return this;
    }

    public Function<GetRequest, Response> get() {
        return this.https.get();
    }

    @Override
    public void close() throws Exception {
        if (https.isConnected().get()) {
            log.debug("Closing client connection");
            disconnect();
        }
    }
}
