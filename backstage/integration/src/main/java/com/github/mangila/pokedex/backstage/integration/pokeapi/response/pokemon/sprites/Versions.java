package com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Versions(
        @JsonProperty("generation-i") GenerationI generationI,
        @JsonProperty("generation-ii") GenerationII generationII,
        @JsonProperty("generation-iii") GenerationIII generationIII,
        @JsonProperty("generation-iv") GenerationIV generationIV,
        @JsonProperty("generation-v") GenerationV generationV,
        @JsonProperty("generation-vi") GenerationVI generationVI,
        @JsonProperty("generation-vii") GenerationVII generationVII,
        @JsonProperty("generation-viii") GenerationVIII generationVIII
) {}
