package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Generation(
        @JsonProperty("name") String name
) {
}
