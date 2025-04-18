package com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OfficialArtwork(
        @JsonProperty("front_default") String frontDefault
) {
}
