package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.JsonResponse;

public final class PokeApiClientUtil {

    private PokeApiClientUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static JsonResponse ensureSuccessStatusCode(JsonResponse jsonResponse) {
        if (jsonResponse.httpStatus().code().startsWith("2")) {
            return jsonResponse;
        } else {
            throw new IllegalStateException("Failed to fetch pokemons");
        }
    }
}
