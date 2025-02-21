package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationII(
        @JsonProperty("crystal") Crystal crystal,
        @JsonProperty("gold") Gold gold,
        @JsonProperty("silver") Silver silver
) {}
