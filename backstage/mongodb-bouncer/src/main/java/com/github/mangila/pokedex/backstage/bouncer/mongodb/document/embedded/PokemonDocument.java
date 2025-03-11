package com.github.mangila.pokedex.backstage.bouncer.mongodb.document.embedded;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

public record PokemonDocument(
        @Field("pokemon_id")
        @Indexed
        int id,
        String name,
        @Field("is_default")
        boolean isDefault,
        String height,
        String weight,
        List<PokemonTypeDocument> types,
        List<PokemonStatDocument> stats,
        List<PokemonMediaDocument> media
) {
}
