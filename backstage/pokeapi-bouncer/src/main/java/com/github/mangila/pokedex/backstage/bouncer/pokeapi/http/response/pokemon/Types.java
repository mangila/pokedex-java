package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Types(
        @JsonProperty("type") Type type
) {}
