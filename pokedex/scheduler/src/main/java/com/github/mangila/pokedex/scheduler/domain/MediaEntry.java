package com.github.mangila.pokedex.scheduler.domain;

import com.github.mangila.pokedex.shared.model.PokeApiUri;

public record MediaEntry(
        Integer speciesId,
        Integer varietyId,
        String name,
        String suffix,
        PokeApiUri uri
) {
    public MediaEntry {
        java.util.Objects.requireNonNull(speciesId, "Species ID cannot be null");
        java.util.Objects.requireNonNull(varietyId, "Variety ID cannot be null");
        java.util.Objects.requireNonNull(name, "Name cannot be null");
        java.util.Objects.requireNonNull(suffix, "Suffix cannot be null");
        java.util.Objects.requireNonNull(uri, "URI cannot be null");
    }
}

