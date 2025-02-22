package com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Cries(
        @JsonProperty("latest") String latest,
        @JsonProperty("legacy") String legacy
) {}
