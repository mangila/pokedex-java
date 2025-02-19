package com.github.mangila.pokedex.backstage.pokemon.service;

import com.github.mangila.pokedex.backstage.shared.integration.PokeApiTemplate;
import org.springframework.stereotype.Service;

@Service
public class PokeApiService {

    private final PokeApiTemplate pokeApiTemplate;

    public PokeApiService(PokeApiTemplate pokeApiTemplate) {
        this.pokeApiTemplate = pokeApiTemplate;
    }
}
