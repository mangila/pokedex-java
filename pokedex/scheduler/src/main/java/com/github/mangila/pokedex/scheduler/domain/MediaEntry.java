package com.github.mangila.pokedex.scheduler.domain;

import java.net.URI;

@lombok.Builder
public record MediaEntry(
        Integer speciesId,
        Integer varietyId,
        String name,
        String suffix,
        URI uri
) {
}
