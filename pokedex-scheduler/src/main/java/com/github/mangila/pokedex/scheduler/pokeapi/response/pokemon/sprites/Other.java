package com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Other(
        @JsonProperty("official-artwork") OfficialArtwork officialArtwork
) {
}
