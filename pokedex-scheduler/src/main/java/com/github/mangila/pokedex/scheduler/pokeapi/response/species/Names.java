package com.github.mangila.pokedex.scheduler.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Names(
        @JsonProperty("language") Language language,
        @JsonProperty("name") String name
) {
}
