package com.github.mangila.pokedex.shared.https.model;

import com.github.mangila.pokedex.shared.json.model.JsonTree;

public record JsonResponse(HttpStatus httpStatus, Headers headers, JsonTree body) {

    public static JsonResponse from(HttpStatus httpStatus, Headers headers, JsonTree body) {
        return new JsonResponse(httpStatus, headers, body);
    }

}