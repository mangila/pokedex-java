package com.github.mangila.scheduler.service;

import com.github.mangila.document.PokemonSpeciesDocument;
import com.github.mangila.repository.PokemonSpeciesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MongoDbService {

    private final PokemonSpeciesRepository pokemonSpeciesRepository;

    public void save(PokemonSpeciesDocument pokemonSpeciesDocument) {
        pokemonSpeciesRepository.save(pokemonSpeciesDocument);
    }
}
