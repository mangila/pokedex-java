package com.github.mangila.pokedex.scheduler.repository.document.embedded;

import org.springframework.data.mongodb.core.mapping.Field;

@lombok.Builder
public record PokemonMediaDocument(
        @Field("media_id")
        String mediaId,
        @Field("file_name")
        String fileName
) {
}