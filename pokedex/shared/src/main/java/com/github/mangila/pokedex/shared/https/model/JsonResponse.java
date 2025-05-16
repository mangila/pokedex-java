package com.github.mangila.pokedex.shared.https.model;

import com.github.mangila.pokedex.shared.json.model.JsonTree;

public record JsonResponse(HttpStatus httpStatus, Headers headers, JsonTree body) {
}