package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationI(
        @JsonProperty("red_blue") RedBlue redBlue,
        @JsonProperty("yellow") Yellow yellow
) {}
