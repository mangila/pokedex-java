package com.github.mangila.pokedex.backstage.shared.model.document.embedded;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

public record PokemonDocument(
        @Field("pokemon_id")
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
