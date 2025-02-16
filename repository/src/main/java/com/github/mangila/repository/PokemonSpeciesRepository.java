package com.github.mangila.repository;

import com.github.mangila.document.PokemonSpeciesDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PokemonSpeciesRepository extends MongoRepository<PokemonSpeciesDocument, String> {
}
