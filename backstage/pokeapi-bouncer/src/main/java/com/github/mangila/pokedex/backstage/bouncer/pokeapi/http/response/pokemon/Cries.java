package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Cries(
        @JsonProperty("latest") String latest,
        @JsonProperty("legacy") String legacy
) {}
