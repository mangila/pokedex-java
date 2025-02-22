package com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Stat(
        @JsonProperty("name") String name
) {}
