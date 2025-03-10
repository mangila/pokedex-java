package com.github.mangila.pokedex.backstage.shared.model.domain;

public enum RedisKeyPrefix {

    EVOLUTION_CHAIN_KEY_PREFIX("pokeapi.co:evolutionchain:"),
    SPECIES_KEY_PREFIX("pokeapi.co:species:"),
    GENERATION_KEY_PREFIX("pokeapi.co:generation:");

    private final String prefix;

    RedisKeyPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
