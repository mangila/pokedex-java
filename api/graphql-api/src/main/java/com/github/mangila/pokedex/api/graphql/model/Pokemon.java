package com.github.mangila.pokedex.api.graphql.model;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

public record Pokemon(
        @Field("pokemon_id")
        int id,
        String name,
        @Field("is_default")
        boolean isDefault,
        String height,
        String weight,
        List<PokemonType> types,
        List<PokemonStat> stats,
        List<PokemonMedia> media
) {
}