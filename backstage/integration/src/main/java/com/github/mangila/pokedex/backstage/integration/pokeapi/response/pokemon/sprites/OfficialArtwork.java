package com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OfficialArtwork(
        @JsonProperty("front_default") String frontDefault,
        @JsonProperty("front_shiny") String frontShiny
) {}
