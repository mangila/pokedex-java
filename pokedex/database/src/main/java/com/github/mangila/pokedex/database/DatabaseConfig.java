package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.util.Ensure;

import java.util.concurrent.TimeUnit;

public record DatabaseConfig(
        DatabaseName databaseName,
        LruCacheConfig lruCacheConfig,
        CompactThreadConfig compactThreadConfig,
        ReaderThreadConfig readerThreadConfig,
        WriteThreadConfig writeThreadConfig
) {
    public DatabaseConfig {
        Ensure.notNull(databaseName, "databaseName must not be null");
        Ensure.notNull(lruCacheConfig, "lruCacheConfig must not be null");
        Ensure.notNull(compactThreadConfig, "compactThreadConfig must not be null");
        Ensure.notNull(readerThreadConfig, "readerThreadConfig must not be null");
        Ensure.notNull(writeThreadConfig, "writeThreadConfig must not be null");
    }

    public record CompactThreadConfig(int initialDelay, int delay, TimeUnit timeUnit) {
        public CompactThreadConfig {
            Ensure.min(1, initialDelay);
            Ensure.min(1, delay);
            Ensure.notNull(timeUnit, "timeUnit must not be null");
        }
    }

    public record ReaderThreadConfig(int nThreads, int permits) {
        public ReaderThreadConfig {
            Ensure.min(1, nThreads);
            Ensure.min(1, permits);
        }
    }

    public record WriteThreadConfig(int permits) {
        public WriteThreadConfig {
            Ensure.min(1, permits);
        }
    }
}
