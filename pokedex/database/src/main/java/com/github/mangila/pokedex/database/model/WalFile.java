package com.github.mangila.pokedex.database.model;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.file.Path;

public record WalFile(Path path) {
    public WalFile {
        Ensure.notNull(path, Path.class);
    }

}
