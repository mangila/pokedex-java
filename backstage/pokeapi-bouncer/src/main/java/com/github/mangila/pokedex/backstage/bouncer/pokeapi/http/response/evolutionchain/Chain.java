package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.evolutionchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.ChainPrototype;

import java.util.List;

public record Chain(
        @JsonProperty("evolves_to")
        List<EvolutionChain> firstChain,
        @JsonProperty("species")
        Species species
) {

    public ChainPrototype toProto() {
        return ChainPrototype.newBuilder()
                .addAllFirstChain(firstChain.stream().map(EvolutionChain::toProto).toList())
                .setSpeciesName(species.name())
                .build();
    }

}
