package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Genera(
        @JsonProperty("language") Language language,
        @JsonProperty("genus") String genus
) {}
