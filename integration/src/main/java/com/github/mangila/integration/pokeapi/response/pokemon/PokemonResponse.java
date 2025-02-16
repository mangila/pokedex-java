package com.github.mangila.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PokemonResponse(
        @JsonProperty("name")
        String name
) {
}
