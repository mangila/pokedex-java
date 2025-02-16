package com.github.mangila.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Variety(
        @JsonProperty("pokemon")
        Pokemon pokemon
) {
}
