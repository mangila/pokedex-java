package com.github.mangila.pokedex.backstage.shared.model.domain;

public enum PokemonStat {
    HP("hp"),
    ATTACK("attack"),
    DEFENSE("defense"),
    SPECIAL_ATTACK("special-attack"),
    SPECIAL_DEFENSE("special-defense"),
    SPEED("speed"),
    TOTAL("total");

    private final String stat;
    private int value;

    PokemonStat(String stat) {
        this.stat = stat;
        this.value = 0;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getStat() {
        return stat;
    }

    public static PokemonStat from(String stat) {
        for (PokemonStat pokemonStat : PokemonStat.values()) {
            if (pokemonStat.getStat().equalsIgnoreCase(stat)) {
                return pokemonStat;
            }
        }
        throw new IllegalArgumentException("Unknown stat: " + stat);
    }
}
