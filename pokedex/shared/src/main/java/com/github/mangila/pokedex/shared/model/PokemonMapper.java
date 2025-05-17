package com.github.mangila.pokedex.shared.model;

import com.github.mangila.pokedex.shared.json.model.JsonTree;

import java.util.List;

public class PokemonMapper {

    public static PokemonVariety toPokemonVariety(JsonTree jsonTree) {
        return new PokemonVariety(jsonTree.getValue("name").getString());
    }

    public static PokemonEvolutionChain toPokemonEvolutionChain(JsonTree jsonTree) {
        var firstChain = jsonTree.getObject("chain");
        if (firstChain.getArray("evolves_to").isEmpty()) {
            return PokemonEvolutionChain.EMPTY;
        }
        int order = 1;
        var pokemonEvolutionChain = new PokemonEvolutionChain();
        pokemonEvolutionChain.addEvolution(new PokemonEvolution(order, firstChain
                .getObject("species")
                .getString("name")));
        order = order + 1;
        var nChain = firstChain.getArray("evolves_to");
        while (true) {
            if (nChain.isEmpty()) {
                return pokemonEvolutionChain;
            }
            var nChainName = nChain.values()
                    .getFirst()
                    .getObject()
                    .getObject("species")
                    .getString("name");
            pokemonEvolutionChain.addEvolution(new PokemonEvolution(order, nChainName));
            nChain = nChain.values().getFirst()
                    .getObject()
                    .getArray("evolves_to");
            order = order + 1;
        }
    }

    public static Pokemon toPokemon(JsonTree pokemonSpecies,
                                    List<PokemonVariety> varieties,
                                    PokemonEvolutionChain evolutionChain) {
        return null;
    }
}
