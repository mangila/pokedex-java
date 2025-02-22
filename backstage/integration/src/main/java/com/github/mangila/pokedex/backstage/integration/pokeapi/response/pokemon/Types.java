package com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Types(
        @JsonProperty("type") Type type
) {}
