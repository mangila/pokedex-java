package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.model.Pokemon;

public class Reader {

    private final PokemonFile file;

    public Reader(PokemonFile file) {
        this.file = file;
    }

    public Pokemon get(Long offset) {
        return null;
    }
}
