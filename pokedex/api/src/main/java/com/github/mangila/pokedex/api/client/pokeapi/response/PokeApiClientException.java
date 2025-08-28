package com.github.mangila.pokedex.api.client.pokeapi.response;

import com.github.mangila.pokedex.shared.https.client.json.JsonResponse;
import org.jspecify.annotations.Nullable;

public class PokeApiClientException extends RuntimeException {

    private final JsonResponse response;

    public PokeApiClientException(String message, Throwable cause, JsonResponse response) {
        super(message, cause);
        this.response = response;
    }

    public PokeApiClientException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public PokeApiClientException(String message) {
        this(message, null, null);
    }


    public PokeApiClientException(String message, JsonResponse response) {
        this(message, null, response);
    }

    public @Nullable JsonResponse getResponse() {
        return response;
    }
}
