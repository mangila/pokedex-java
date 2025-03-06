package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationIII(
        @JsonProperty("emerald") Emerald emerald,
        @JsonProperty("firered_leafgreen") FireredLeafgreen fireredLeafgreen,
        @JsonProperty("ruby_sapphire") RubySapphire rubySapphire
) {}
