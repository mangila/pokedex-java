package com.github.mangila.pokedex.backstage.integration.pokeapi.response.evolutionchain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvolutionChain(
        @JsonProperty("evolves_to")
        EvolutionChain[] nextChain,
        @JsonProperty("species")
        Species species
) {}
