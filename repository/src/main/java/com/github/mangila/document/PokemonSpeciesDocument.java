package com.github.mangila.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("pokemon-species")
@lombok.Data
public class PokemonSpeciesDocument {
    @Id
    private Integer id;
    @Field("name")
    @Indexed(unique = true)
    private String name;
}
