package com.github.mangila.pokedex.shared.repository.document.embedded;


import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.Stats;

import java.util.List;

public record PokemonStatDocument(
        String name,
        int value
) {

    public static PokemonStatDocument fromStat(Stats stats) {
        return new PokemonStatDocument(stats.stat().name(), stats.baseStat());
    }

    public static List<PokemonStatDocument> fromStatsWithTotal(List<Stats> stats) {
        var totalSum = stats.stream()
                .mapToInt(Stats::baseStat)
                .sum();
        var total = new PokemonStatDocument("total", totalSum);
        var list = new java.util.ArrayList<>(stats.stream()
                .map(PokemonStatDocument::fromStat)
                .toList());
        list.add(total);
        return list;
    }
}
