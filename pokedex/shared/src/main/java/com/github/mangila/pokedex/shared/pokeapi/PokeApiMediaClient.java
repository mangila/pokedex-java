package com.github.mangila.pokedex.shared.pokeapi;

public class PokeApiMediaClient extends HttpsClient {

    public PokeApiMediaClient(String host) {
        super(host);
    }

    @Override
    public Response execute(Request request) {
        return null;
    }
}
