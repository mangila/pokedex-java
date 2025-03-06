package com.github.mangila.pokedex.backstage.integration.pokeapi.response.generation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.redis.SpeciesPrototype;

public record Species(
        @JsonProperty("name")
        String name
) {

    public static Species fromProto(SpeciesPrototype proto) {
        return new Species(proto.getName());
    }

    public SpeciesPrototype toProto() {
        return SpeciesPrototype.newBuilder()
                .setName(name)
                .build();
    }
}
