package com.github.mangila.pokedex.api.graphql.service;

import com.github.mangila.pokedex.api.graphql.model.PokemonSpecies;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@lombok.AllArgsConstructor
public class PokemonService {

    private final MongoTemplate mongoTemplate;

    public PokemonSpecies findById(Integer id) {
        return mongoTemplate.findById(id, PokemonSpecies.class,"pokemon-species");
    }

    public PokemonSpecies findByName(String name) {
        return mongoTemplate.findOne(new Query(Criteria.where("name").is(name)), PokemonSpecies.class);
    }
}
