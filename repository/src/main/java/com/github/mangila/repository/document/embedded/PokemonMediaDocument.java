package com.github.mangila.repository.document.embedded;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

@lombok.Data
public class PokemonMediaDocument {
    @Field("media_id")
    @Indexed
    private String mediaId;
    @Field("src")
    @Indexed
    private String src;
    @Field("file_name")
    private String fileName;
    @Field("content_type")
    private String contentType;
}
