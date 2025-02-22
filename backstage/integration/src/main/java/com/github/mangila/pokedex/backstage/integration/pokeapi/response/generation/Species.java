package com.github.mangila.pokedex.backstage.integration.pokeapi.response.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record Species(
        @JsonProperty("name")
        String name
) implements Serializable {
}
