package com.github.mangila.pokedex.backstage.bouncer.mongodb.document.embedded;

import org.springframework.data.mongodb.core.mapping.Field;

public record PokemonMediaDocument(
        @Field("media_id")
        String mediaId,
        @Field("src")
        String src,
        @Field("file_name")
        String fileName
) {
}