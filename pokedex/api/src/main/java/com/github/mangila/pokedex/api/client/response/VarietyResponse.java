package com.github.mangila.pokedex.api.client.response;

import com.github.mangila.pokedex.shared.json.model.JsonRoot;

public record VarietyResponse(String name, int height, int weight) {
    public static VarietyResponse from(JsonRoot jsonRoot) {
        String name = jsonRoot.getValue("name").unwrapString();
        int height = jsonRoot.getValue("height").unwrapNumber().intValue();
        int weight = jsonRoot.getValue("weight").unwrapNumber().intValue();
        return new VarietyResponse(name, height, weight);
    }

}
