package com.github.mangila.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PokemonResponse(
        @JsonProperty("id")
        Integer id,
        @JsonProperty("name")
        String name,
        @JsonProperty("sprites")
        Sprites sprites,
        @JsonProperty("cries")
        Cries cries
) {
}
