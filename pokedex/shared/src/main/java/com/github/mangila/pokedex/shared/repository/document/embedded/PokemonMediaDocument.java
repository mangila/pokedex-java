package com.github.mangila.pokedex.shared.repository.document.embedded;

import org.springframework.data.mongodb.core.mapping.Field;

public record PokemonMediaDocument(
        @Field("media_id")
        String mediaId,
        @Field("file_name")
        String fileName
) {
}
