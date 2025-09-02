package com.github.mangila.pokedex.database.config;

public record WalConfig(int thresholdWriteLimit,
                        int walFileSize) {
}
