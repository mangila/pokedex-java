package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.database.internal.io.DatabaseFileName;

import java.nio.file.Path;
import java.nio.file.Paths;

public record DatabaseFile(Path path) {
    public DatabaseFile(DatabaseFileName databaseFileName) {
        this(Paths.get(databaseFileName.value()));
    }
}
