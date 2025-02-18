package com.github.mangila.pokedex.api.shared.repository;

import com.github.mangila.pokedex.api.shared.repository.document.PokemonSpeciesDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PokemonSpeciesRepository extends MongoRepository<PokemonSpeciesDocument, String> {
}
