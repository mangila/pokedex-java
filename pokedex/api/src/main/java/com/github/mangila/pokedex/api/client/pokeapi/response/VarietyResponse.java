package com.github.mangila.pokedex.api.client.pokeapi.response;

import com.github.mangila.pokedex.shared.json.model.JsonRoot;

import java.math.BigInteger;

public record VarietyResponse(String name, BigInteger height, BigInteger weight) {
    public static VarietyResponse from(JsonRoot jsonRoot) {
        String name = jsonRoot.getValue("name").unwrapString();
        BigInteger height = (BigInteger) jsonRoot.getNumber("height");
        BigInteger weight = (BigInteger) jsonRoot.getNumber("weight");
        return new VarietyResponse(name, height, weight);
    }

}
