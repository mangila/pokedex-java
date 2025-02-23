package com.github.mangila.pokedex.backstage.integration.pokeapi.response.evolutionchain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Chain(
        @JsonProperty("evolves_to")
        EvolutionChain[] firstChain,
        @JsonProperty("species")
        Species species
) {}
