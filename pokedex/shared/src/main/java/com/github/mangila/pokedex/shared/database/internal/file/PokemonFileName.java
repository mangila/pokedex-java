package com.github.mangila.pokedex.shared.database.internal.file;

public record PokemonFileName(String value) {

    public PokemonFileName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("database value must not be null or blank");
        }
        if (!value.endsWith(".pokemon")) {
            throw new IllegalArgumentException("database value must end with .pokemon");
        }
    }

}
