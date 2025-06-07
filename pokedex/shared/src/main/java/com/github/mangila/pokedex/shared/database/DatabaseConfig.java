package com.github.mangila.pokedex.shared.database;

import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;

import java.util.concurrent.TimeUnit;

public record DatabaseConfig(
        DatabaseName databaseName,
        LruCacheConfig lruCacheConfig,
        CompactThreadConfig compactThreadConfig,
        ReaderThreadConfig readerThreadConfig,
        WriteThreadConfig writeThreadConfig
) {

    public record CompactThreadConfig(int initialDelay, int delay, TimeUnit timeUnit) {
        public CompactThreadConfig {
            if (initialDelay < 0) {
                throw new IllegalArgumentException("initialDelay must be greater than or equal to 0");
            }
            if (delay < 0) {
                throw new IllegalArgumentException("delay must be greater than or equal to 0");
            }
            if (timeUnit == null) {
                throw new IllegalArgumentException("timeUnit must not be null");
            }
        }
    }

    public record ReaderThreadConfig(int nThreads, int permits) {
        public ReaderThreadConfig {
            if (nThreads <= 0) {
                throw new IllegalArgumentException("nThreads must be greater than 0");
            }
            if (permits <= 0) {
                throw new IllegalArgumentException("permits must be greater than 0");
            }
        }
    }

    public record WriteThreadConfig(int permits) {
        public WriteThreadConfig {
            if (permits <= 0) {
                throw new IllegalArgumentException("permits must be greater than 0");
            }
        }
    }
}
