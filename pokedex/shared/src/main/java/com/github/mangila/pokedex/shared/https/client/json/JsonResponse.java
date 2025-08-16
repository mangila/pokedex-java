package com.github.mangila.pokedex.shared.https.client.json;

import com.github.mangila.pokedex.shared.https.model.Body;
import com.github.mangila.pokedex.shared.https.model.ResponseHeaders;
import com.github.mangila.pokedex.shared.https.model.Status;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;

public record JsonResponse(Status status, ResponseHeaders responseHeaders, JsonRoot body) {

    public static JsonResponse from(Status status, ResponseHeaders responseHeaders, Body body, JsonParser parser) {
        JsonRoot jsonRoot = parser.parseTree(body.value());
        return new JsonResponse(status, responseHeaders, jsonRoot);
    }

    public boolean isSuccess() {
        return status.code().startsWith("2");
    }
}