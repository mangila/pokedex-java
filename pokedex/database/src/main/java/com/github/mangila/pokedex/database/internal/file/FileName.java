package com.github.mangila.pokedex.database.internal.file;

public record FileName(String value) {

    public FileName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("database value must not be null or blank");
        }
        if (!value.endsWith(".yakvs")) {
            throw new IllegalArgumentException("database value must endOffset with .db");
        }
    }

}
