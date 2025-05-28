package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.JsonResponse;

public final class PokeApiClientUtil {

    private PokeApiClientUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * <summary>
     * Ensure that the response status code is 2xx <br>
     * Ensure Pattern / Fail fast
     * </summary>
     */
    public static JsonResponse ensureSuccessStatusCode(JsonResponse jsonResponse) {
        if (!jsonResponse.httpStatus().code().startsWith("2")) {
            throw new IllegalStateException("Failed to fetch pokemons");
        }
        return jsonResponse;
    }

    /**
     * CR (Carriage Return): ASCII value 13 (\r)
     * LF (Line Feed): ASCII value 10 (\n)
     */
    public static boolean isCrLf(int carriageReturn, int lineFeed) {
        return carriageReturn == '\r' && lineFeed == '\n';
    }
}
