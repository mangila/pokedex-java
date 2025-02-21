package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Genera(
        @JsonProperty("language") Language language,
        @JsonProperty("genus") String genus
) {}
