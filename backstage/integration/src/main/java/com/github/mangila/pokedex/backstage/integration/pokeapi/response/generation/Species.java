package com.github.mangila.pokedex.backstage.integration.pokeapi.response.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Species(
        @JsonProperty("name")
        String name
) {
}
