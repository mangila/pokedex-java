package com.github.mangila.integration.pokeapi.response.evolutionchain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvolutionChainResponse(
        @JsonProperty("id")
        Integer id,
        @JsonProperty("chain")
        Chain chain
) {
}
