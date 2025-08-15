package com.github.mangila.pokedex.api.client.response;

import com.github.mangila.pokedex.shared.json.model.JsonTree;

public record VarietyResponse(String name, int height, int weight) {
    public static VarietyResponse from(JsonTree jsonTree) {
        String name = jsonTree.getValue("name").getString();
        int height = jsonTree.getValue("height").getNumber().intValue();
        int weight = jsonTree.getValue("weight").getNumber().intValue();
        return new VarietyResponse(name, height, weight);
    }

}
