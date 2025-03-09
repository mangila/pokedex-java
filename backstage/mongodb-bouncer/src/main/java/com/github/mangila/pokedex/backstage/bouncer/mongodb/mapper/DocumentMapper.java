package com.github.mangila.pokedex.backstage.bouncer.mongodb.mapper;

import com.github.mangila.pokedex.backstage.bouncer.mongodb.document.PokemonSpeciesDocument;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.pokemonspecies.PokemonSpecies;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    public PokemonSpeciesDocument toDocument(PokemonSpecies request) {
        return null;
    }
}
