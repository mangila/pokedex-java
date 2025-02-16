package com.github.mangila.integration.pokeapi.response.evolutionchain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Chain(
        @JsonProperty("evolves_to")
        List<Chain> chain,
        @JsonProperty("species")
        Species species
) {
}
