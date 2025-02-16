package com.github.mangila.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Pokemon(
        @JsonProperty("name")
        String name
) {
}
