package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Other(
        @JsonProperty("dream_world") DreamWorld dreamWorld,
        @JsonProperty("home") Home home,
        @JsonProperty("official-artwork") OfficialArtwork officialArtwork,
        @JsonProperty("showdown") Showdown showdown
) {}
