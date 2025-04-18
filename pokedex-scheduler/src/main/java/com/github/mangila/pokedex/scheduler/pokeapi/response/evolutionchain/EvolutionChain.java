package com.github.mangila.pokedex.scheduler.pokeapi.response.evolutionchain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EvolutionChain(
        @JsonProperty("evolves_to")
        List<EvolutionChain> nextChain,
        @JsonProperty("species")
        Species species
) {
}
