package com.github.mangila.pokedex.shared.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Pokemon(
        @JsonProperty("name") String name,
        @JsonProperty("url") String url
) {
}
