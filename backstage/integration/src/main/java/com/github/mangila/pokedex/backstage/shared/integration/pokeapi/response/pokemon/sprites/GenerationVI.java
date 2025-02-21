package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationVI(
        @JsonProperty("omegaruby_alphasapphire") OmegarubyAlphasapphire omegarubyAlphasapphire,
        @JsonProperty("x_y") XY xy
) {}
