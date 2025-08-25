package com.github.mangila.pokedex.scheduler.model;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri;

public record EvolutionChainUrl(
        PokeApiUri pokeApiUri,
        String key
) {
}
