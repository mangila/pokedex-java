package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Species(
        @JsonProperty("name")
        String name
) {
}
