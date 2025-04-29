package com.github.mangila.pokedex.shared.pokeapi;

public class PokeApiMediaClient extends HttpsClient {

    public PokeApiMediaClient(String host) {
        super(host);
    }

    @Override
    Response get(GetRequest getRequest) {
        return null;
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }
}
