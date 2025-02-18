package com.github.mangila.pokedex.api.shared.repository.document;

import com.github.mangila.pokedex.api.shared.repository.document.embedded.PokemonVarietyDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document("pokemon-species")
@lombok.Data
public class PokemonSpeciesDocument {
    @Id
    private Integer id;
    @Field("name")
    @Indexed
    private String name;
    @Field("varieties")
    private List<PokemonVarietyDocument> varieties;
}
