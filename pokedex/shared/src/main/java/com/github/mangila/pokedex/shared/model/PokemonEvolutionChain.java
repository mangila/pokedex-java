package com.github.mangila.pokedex.shared.model;

import java.util.ArrayList;
import java.util.List;

public class PokemonEvolutionChain {

    public static final PokemonEvolutionChain EMPTY = new PokemonEvolutionChain();

    private final List<PokemonEvolution> evolutions;

    public PokemonEvolutionChain() {
        this.evolutions = new ArrayList<>();
    }

    public void addEvolution(PokemonEvolution evolution) {
        this.evolutions.add(evolution);
    }

}
