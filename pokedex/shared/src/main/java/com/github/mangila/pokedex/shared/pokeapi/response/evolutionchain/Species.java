package com.github.mangila.pokedex.shared.pokeapi.response.evolutionchain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Species(
        @JsonProperty("name")
        String name
) {
}
