package com.github.mangila.pokedex.backstage.shared.model.domain;

public enum PokemonType {

    NORMAL("normal"),
    FIGHTING("fighting"),
    FLYING("flying"),
    POISON("poison"),
    GROUND("ground"),
    ROCK("rock"),
    BUG("bug"),
    GHOST("ghost"),
    STEEL("steel"),
    FIRE("fire"),
    WATER("water"),
    GRASS("grass"),
    ELECTRIC("electric"),
    PSYCHIC("psychic"),
    ICE("ice"),
    DRAGON("dragon"),
    DARK("dark"),
    FAIRY("fairy");

    private final String type;

    PokemonType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static PokemonType from(String type) {
        for (PokemonType pokemonType : PokemonType.values()) {
            if (pokemonType.getType().equalsIgnoreCase(type)) {
                return pokemonType;
            }
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }
}
