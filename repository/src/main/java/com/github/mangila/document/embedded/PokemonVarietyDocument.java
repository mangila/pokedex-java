package com.github.mangila.document.embedded;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@lombok.Data
public class PokemonVarietyDocument {
    @Field("variety_id")
    @Indexed(unique = true)
    private Integer varietyId;
    @Field("name")
    @Indexed(unique = true)
    private String name;
    @Field("images")
    private List<PokemonMediaDocument> images;
    @Field("audios")
    private List<PokemonMediaDocument> audios;
}
