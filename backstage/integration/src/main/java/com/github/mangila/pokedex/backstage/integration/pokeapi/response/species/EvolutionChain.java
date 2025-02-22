package com.github.mangila.pokedex.backstage.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvolutionChain(
        @JsonProperty("url") String url
) {}
