package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DreamWorld(
        @JsonProperty("front_default") String frontDefault,
        @JsonProperty("front_female") String frontFemale
) {}
