package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.evolutionchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.EvolutionChainPrototype;

import java.util.List;

public record EvolutionChain(
        @JsonProperty("evolves_to")
        List<EvolutionChain> nextChain,
        @JsonProperty("species")
        Species species
) {
    public EvolutionChainPrototype toProto() {
        return EvolutionChainPrototype.newBuilder()
                .addAllNextChain(nextChain.stream().map(EvolutionChain::toProto).toList())
                .setSpeciesName(species.name())
                .build();
    }
}
