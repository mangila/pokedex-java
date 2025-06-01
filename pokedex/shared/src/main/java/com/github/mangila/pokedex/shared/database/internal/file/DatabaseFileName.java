package com.github.mangila.pokedex.shared.database.internal.file;

public record DatabaseFileName(String value) {

    public DatabaseFileName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("database value must not be null or blank");
        }
        if (!value.endsWith(".yakvs")) {
            throw new IllegalArgumentException("database value must end with .db");
        }
    }

}
