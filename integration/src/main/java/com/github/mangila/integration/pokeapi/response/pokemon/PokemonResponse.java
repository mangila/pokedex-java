package com.github.mangila.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PokemonResponse(
        @JsonProperty("id")
        Integer id,
        @JsonProperty("name")
        String name,
        @JsonProperty("is_default")
        Boolean isDefault,
        @JsonProperty("sprites")
        Sprites sprites,
        @JsonProperty("cries")
        Cries cries
) {
}
