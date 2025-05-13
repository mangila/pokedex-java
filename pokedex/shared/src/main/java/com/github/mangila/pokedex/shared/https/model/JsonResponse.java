package com.github.mangila.pokedex.shared.https.model;

import com.github.mangila.pokedex.shared.json.model.JsonTree;

import java.util.Map;
import java.util.Objects;

public record JsonResponse(HttpStatus httpStatus,
                           Map<String, String> headers,
                           JsonTree body) {
    public JsonResponse {
        Objects.requireNonNull(httpStatus, "httpStatus must not be null");
        Objects.requireNonNull(headers, "headers must not be null");
        Objects.requireNonNull(body, "body must not be null");
    }
}
