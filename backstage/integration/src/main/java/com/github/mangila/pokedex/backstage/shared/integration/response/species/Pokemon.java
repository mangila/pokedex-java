package com.github.mangila.pokedex.backstage.shared.integration.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Pokemon(
        @JsonProperty("name") String name,
        @JsonProperty("url") String url
) {
}
