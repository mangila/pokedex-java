package com.github.mangila.pokedex.backstage.bouncer.pokeapi.mapper;

import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.PokemonResponse;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.species.PokemonSpeciesResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Map pokeapi http responses to proto models
 */
@Component
public class PokeApiProtoMapper {

    private static final Matcher REPLACE_LINE_BREAKS = Pattern.compile("[\r\n\t\f]+").matcher(Strings.EMPTY);

    public com.github.mangila.pokedex.backstage.model.grpc.pokeapi.generation.GenerationResponse map(GenerationResponse generationResponse) {
        return null;
    }

    public com.github.mangila.pokedex.backstage.model.grpc.pokeapi.species.PokemonSpeciesResponse map(PokemonSpeciesResponse pokemonSpeciesResponse) {
        return null;
    }

    public com.github.mangila.pokedex.backstage.model.grpc.pokeapi.evolutionchain.EvolutionChainResponse map(EvolutionChainResponse evolutionChainResponse) {
        return null;
    }

    public com.github.mangila.pokedex.backstage.model.grpc.pokeapi.pokemon.PokemonResponse map(PokemonResponse pokemonResponse) {
        return null;
    }
}
