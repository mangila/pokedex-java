package com.github.mangila.pokedex.scheduler.domain;

import java.net.URI;

@lombok.Builder
public record PokemonEntry(
        String name,
        URI uri
) {
}
