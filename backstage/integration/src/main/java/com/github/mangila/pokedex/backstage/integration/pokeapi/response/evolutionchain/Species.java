package com.github.mangila.pokedex.backstage.integration.pokeapi.response.evolutionchain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Species(
        @JsonProperty("name")
        String name
) {}
