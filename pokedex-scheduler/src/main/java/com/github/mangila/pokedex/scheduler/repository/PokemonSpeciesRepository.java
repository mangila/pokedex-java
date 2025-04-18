package com.github.mangila.pokedex.scheduler.repository;

import com.github.mangila.pokedex.scheduler.repository.document.PokemonSpeciesDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PokemonSpeciesRepository extends MongoRepository<PokemonSpeciesDocument, Integer> {
}
