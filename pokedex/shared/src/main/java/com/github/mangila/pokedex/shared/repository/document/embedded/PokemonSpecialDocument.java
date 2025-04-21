package com.github.mangila.pokedex.shared.repository.document.embedded;

import org.springframework.data.mongodb.core.mapping.Field;

@lombok.Builder
public record PokemonSpecialDocument(
        @Field("is_special")
        boolean isSpecial,
        boolean legendary,
        boolean mythical,
        boolean baby
) {
}
