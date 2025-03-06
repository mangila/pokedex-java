package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.evolutionchain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Species(
        @JsonProperty("name")
        String name
) {
}
