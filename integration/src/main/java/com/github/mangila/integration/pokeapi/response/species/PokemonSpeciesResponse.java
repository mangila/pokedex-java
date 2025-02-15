package com.github.mangila.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PokemonSpeciesResponse(
        @JsonProperty("id")
        Integer id,
        @JsonProperty("name")
        String name
) {
}
