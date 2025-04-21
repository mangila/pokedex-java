package com.github.mangila.pokedex.shared.repository.document.embedded;

import org.springframework.util.CollectionUtils;
import com.github.mangila.pokedex.shared.pokeapi.response.evolutionchain.EvolutionChain;
import com.github.mangila.pokedex.shared.pokeapi.response.evolutionchain.EvolutionChainResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@lombok.Builder
public record PokemonEvolutionDocument(
        int order,
        String name
) {

    public static List<PokemonEvolutionDocument> of(EvolutionChainResponse response) {
        if (CollectionUtils.isEmpty(response.chain().firstChain())) {
            return Collections.emptyList();
        }
        var chain = response.chain();
        return getEvolutions(chain.firstChain(), new ArrayList<>(
                List.of(PokemonEvolutionDocument.builder()
                        .order(0)
                        .name(chain.species().name())
                        .build())
        ));
    }

    private static List<PokemonEvolutionDocument> getEvolutions(List<EvolutionChain> next,
                                                                ArrayList<PokemonEvolutionDocument> evolutions) {
        while (true) {
            if (CollectionUtils.isEmpty(next)) {
                return evolutions;
            }
            var chain = next.getFirst();
            evolutions.add(PokemonEvolutionDocument.builder()
                    .order(evolutions.size())
                    .name(chain.species().name())
                    .build());
            next = chain.nextChain();
        }
    }
}
