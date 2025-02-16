package com.github.mangila.scheduler.mapper;

import com.github.mangila.document.PokemonSpeciesDocument;
import com.github.mangila.integration.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.integration.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.integration.pokeapi.response.species.PokemonSpeciesResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PokeApiMapper {

    public PokemonSpeciesDocument ToDocument(PokemonSpeciesResponse speciesResponse,
                                             EvolutionChainResponse evolutionChain,
                                             List<PokemonResponse> varieties) {
        var document = new PokemonSpeciesDocument();
        document.setId(speciesResponse.id());
        document.setName(speciesResponse.name());
        return document;
    }
}
