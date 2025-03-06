package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.CriesPrototype;

public record Cries(
        @JsonProperty("latest") String latest,
        @JsonProperty("legacy") String legacy
) {
    public CriesPrototype toProto() {
        return CriesPrototype.newBuilder()
                .setLatest(latest)
                .setLegacy(legacy)
                .build();
    }
}
