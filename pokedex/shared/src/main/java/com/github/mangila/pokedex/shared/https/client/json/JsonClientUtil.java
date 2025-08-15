package com.github.mangila.pokedex.shared.https.client.json;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.util.function.UnaryOperator;

public final class JsonClientUtil {

    private JsonClientUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void ensureSuccess(JsonResponse jsonResponse) {
        Ensure.notNull(jsonResponse, JsonResponse.class);
        if (!jsonResponse.isSuccess()) {
            throw new IllegalStateException("Failed to fetch pokemons");
        }
    }

    public static UnaryOperator<JsonResponse> ensureSuccess() {
        return jsonResponse -> {
            ensureSuccess(jsonResponse);
            return jsonResponse;
        };
    }
}
