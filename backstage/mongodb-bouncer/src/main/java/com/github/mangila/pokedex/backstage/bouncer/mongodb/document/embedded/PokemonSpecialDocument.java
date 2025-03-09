package com.github.mangila.pokedex.backstage.bouncer.mongodb.document.embedded;

import org.springframework.data.mongodb.core.mapping.Field;

public record PokemonSpecialDocument(
        @Field("is_special")
        boolean isSpecial,
        boolean legendary,
        boolean mythical,
        boolean baby
) {
}
