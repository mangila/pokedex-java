package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvolutionChain(
        @JsonProperty("url") String url
) {
}
