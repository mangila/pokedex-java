package com.github.mangila.integration.pokeapi.response.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PokemonSpecies(
        @JsonProperty("name")
        String name
) {
}
