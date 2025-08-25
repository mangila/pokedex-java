package com.github.mangila.pokedex.database.wal;

import java.nio.file.Path;

class WalFile {

    private final Path path;

    WalFile(Path path) {
        this.path = path;
    }
}
