package com.github.mangila.pokedex.shared.repository;

import com.github.mangila.pokedex.shared.repository.document.PokemonSpeciesDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PokemonSpeciesRepository extends MongoRepository<PokemonSpeciesDocument, Integer> {
}
