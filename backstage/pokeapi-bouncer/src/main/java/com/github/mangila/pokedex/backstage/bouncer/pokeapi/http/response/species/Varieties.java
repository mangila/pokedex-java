package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Varieties(
        @JsonProperty("pokemon") Pokemon pokemon
) {
}
