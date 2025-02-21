package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationIV(
        @JsonProperty("diamond_pearl") DiamondPearl diamondPearl,
        @JsonProperty("heartgold_soulsilver") HeartgoldSoulsilver heartgoldSoulsilver,
        @JsonProperty("platinum") Platinum platinum
) {}
