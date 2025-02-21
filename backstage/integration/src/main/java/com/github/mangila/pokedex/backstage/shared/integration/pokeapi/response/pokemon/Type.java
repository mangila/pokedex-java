package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Type(
        @JsonProperty("name") String name
) {}
