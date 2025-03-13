package com.github.mangila.pokedex.api.graphql.model;

import org.springframework.data.mongodb.core.mapping.Field;

public record PokemonMedia(
        @Field("media_id")
        String mediaId,
        @Field("src")
        String src,
        @Field("file_name")
        String fileName
) {
}
