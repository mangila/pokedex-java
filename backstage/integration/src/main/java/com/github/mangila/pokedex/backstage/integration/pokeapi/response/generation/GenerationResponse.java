package com.github.mangila.pokedex.backstage.integration.pokeapi.response.generation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.redis.GenerationResponsePrototype;

import java.util.List;

public record GenerationResponse(
        @JsonProperty("pokemon_species")
        List<Species> pokemonSpecies
) {

    public static GenerationResponse fromProto(GenerationResponsePrototype proto) {
        return new GenerationResponse(proto.getPokemonSpeciesList()
                .stream()
                .map(Species::fromProto)
                .toList());
    }

    public GenerationResponsePrototype toProto() {
        return GenerationResponsePrototype.newBuilder()
                .addAllPokemonSpecies(pokemonSpecies.stream()
                        .map(Species::toProto)
                        .toList())
                .build();
    }

}
