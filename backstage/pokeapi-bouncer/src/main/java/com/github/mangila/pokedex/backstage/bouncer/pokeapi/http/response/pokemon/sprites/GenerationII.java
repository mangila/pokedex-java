package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationII(
        @JsonProperty("crystal") Crystal crystal,
        @JsonProperty("gold") Gold gold,
        @JsonProperty("silver") Silver silver
) {}
