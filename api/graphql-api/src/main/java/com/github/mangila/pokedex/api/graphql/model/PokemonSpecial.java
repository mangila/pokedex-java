package com.github.mangila.pokedex.api.graphql.model;

import org.springframework.data.mongodb.core.mapping.Field;

public record PokemonSpecial(
        @Field("is_special")
        boolean isSpecial,
        boolean legendary,
        boolean mythical,
        boolean baby
) {
}
