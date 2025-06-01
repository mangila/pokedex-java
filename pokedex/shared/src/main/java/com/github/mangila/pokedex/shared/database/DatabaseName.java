package com.github.mangila.pokedex.shared.database;

public record DatabaseName(String value) {

    public DatabaseName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("database value must not be null or blank");
        }
    }

}
