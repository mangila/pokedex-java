package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Other(
        @JsonProperty("dream_world") DreamWorld dreamWorld,
        @JsonProperty("home") Home home,
        @JsonProperty("official-artwork") OfficialArtwork officialArtwork,
        @JsonProperty("showdown") Showdown showdown
) {}
