package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Stats(
        @JsonProperty("base_stat") int baseStat,
        @JsonProperty("stat") Stat stat
) {
}
