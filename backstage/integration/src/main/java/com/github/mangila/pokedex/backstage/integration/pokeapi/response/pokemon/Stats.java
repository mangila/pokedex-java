package com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Stats(
        @JsonProperty("base_stat") int baseStat,
        @JsonProperty("stat") Stat stat
) {}
