package com.github.mangila.pokedex.backstage.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Language(
        @JsonProperty("name") String name
) {}
