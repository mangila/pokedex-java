package com.github.mangila.pokedex.database.internal.io;

import com.github.mangila.pokedex.shared.util.Ensure;

public record DatabaseFileName(String value) {

    public static final String YAKVS_SUFFIX = ".yakvs";

    public DatabaseFileName {
        Ensure.notNull(value, "database name must not be null");
        Ensure.notBlank(value, DatabaseFileName.class);
        if (!value.endsWith(YAKVS_SUFFIX)) {
            throw new IllegalArgumentException("database value must have suffix: %s".formatted(YAKVS_SUFFIX));
        }
    }

}
