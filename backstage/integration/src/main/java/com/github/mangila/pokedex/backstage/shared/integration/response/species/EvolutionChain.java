package com.github.mangila.pokedex.backstage.shared.integration.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvolutionChain(
        @JsonProperty("url") String url
) {}
