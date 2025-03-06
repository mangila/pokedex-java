package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.generation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.GenerationResponsePrototype;

import java.util.List;

public record GenerationResponse(
        @JsonProperty("pokemon_species")
        List<Species> pokemonSpecies
) {

    public GenerationResponsePrototype toProto() {
        return GenerationResponsePrototype.newBuilder()
                .addAllName(pokemonSpecies.stream()
                        .map(Species::name)
                        .toList())
                .build();
    }

}
