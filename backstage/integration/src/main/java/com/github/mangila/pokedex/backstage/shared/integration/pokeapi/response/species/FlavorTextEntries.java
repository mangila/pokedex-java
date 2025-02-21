package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FlavorTextEntries(
        @JsonProperty("flavor_text") String flavorText,
        @JsonProperty("language") Language language
) {}
