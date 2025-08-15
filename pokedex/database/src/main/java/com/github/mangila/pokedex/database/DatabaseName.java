package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.shared.util.Ensure;

public record DatabaseName(String value) {
    public DatabaseName {
        Ensure.notNull(value, "database value must not be null");
        Ensure.notBlank(value, String.class);
    }
}
