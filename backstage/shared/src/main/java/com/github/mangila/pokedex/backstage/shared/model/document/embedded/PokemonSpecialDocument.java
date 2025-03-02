package com.github.mangila.pokedex.backstage.shared.model.document.embedded;

import org.springframework.data.mongodb.core.mapping.Field;

public record PokemonSpecialDocument(
        @Field("is_special")
        boolean isSpecial,
        boolean legendary,
        boolean mythical,
        boolean baby
) {
}
