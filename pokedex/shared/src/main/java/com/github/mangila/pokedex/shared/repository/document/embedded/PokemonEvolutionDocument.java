package com.github.mangila.pokedex.shared.repository.document.embedded;

import org.springframework.util.CollectionUtils;
import com.github.mangila.pokedex.shared.pokeapi.response.evolutionchain.EvolutionChain;
import com.github.mangila.pokedex.shared.pokeapi.response.evolutionchain.EvolutionChainResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record PokemonEvolutionDocument(
        int order,
        String name
) {

    public static List<PokemonEvolutionDocument> fromEvolutionChainResponse(EvolutionChainResponse response) {
        if (CollectionUtils.isEmpty(response.chain().firstChain())) {
            return Collections.emptyList();
        }
        var chain = response.chain();
        return getEvolutions(chain.firstChain(), new ArrayList<>(
                List.of(new PokemonEvolutionDocument(0, chain.species().name()))
        ));
    }

    private static List<PokemonEvolutionDocument> getEvolutions(List<EvolutionChain> next,
                                                                ArrayList<PokemonEvolutionDocument> evolutions) {
        while (true) {
            if (CollectionUtils.isEmpty(next)) {
                return evolutions;
            }
            var chain = next.getFirst();
            evolutions.add(new PokemonEvolutionDocument(evolutions.size(), chain.species().name()));
            next = chain.nextChain();
        }
    }
}
