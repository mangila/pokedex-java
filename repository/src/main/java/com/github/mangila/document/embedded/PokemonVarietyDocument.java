package com.github.mangila.document.embedded;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@lombok.Data
public class PokemonVarietyDocument {
    @Field("variety_id")
    @Indexed
    private Integer varietyId;
    @Field("variety_name")
    @Indexed
    private String varietyName;
    @Field("images")
    private List<PokemonMediaDocument> images;
    @Field("audios")
    private List<PokemonMediaDocument> audios;
}
