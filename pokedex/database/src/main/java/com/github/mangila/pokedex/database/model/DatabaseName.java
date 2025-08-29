package com.github.mangila.pokedex.database.model;

import com.github.mangila.pokedex.shared.util.Ensure;

public record DatabaseName(String value) {
    public DatabaseName {
        Ensure.notNull(value, "database entry must not be null");
        Ensure.notBlank(value, "database entry must not be blank");
    }
}
