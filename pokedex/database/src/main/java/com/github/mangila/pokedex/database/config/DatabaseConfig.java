package com.github.mangila.pokedex.database.config;

import com.github.mangila.pokedex.database.model.DatabaseName;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.util.Ensure;

public record DatabaseConfig(
        DatabaseName databaseName,
        WalConfig walConfig,
        LruCacheConfig lruCacheConfig) {

    private DatabaseConfig(Builder builder) {
        this(builder.databaseName, builder.walConfig, builder.lruCacheConfig);
        Ensure.notNull(databaseName, "database name must not be null");
        Ensure.notNull(walConfig, "wal config must not be null");
        Ensure.notNull(lruCacheConfig, "lru cache config must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DatabaseName databaseName;
        private WalConfig walConfig = new WalConfig(1024);
        private LruCacheConfig lruCacheConfig = new LruCacheConfig(100);

        private Builder() {
        }

        public Builder databaseName(DatabaseName databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder walConfig(WalConfig walConfig) {
            this.walConfig = walConfig;
            return this;
        }

        public Builder lruCacheConfig(LruCacheConfig lruCacheConfig) {
            this.lruCacheConfig = lruCacheConfig;
            return this;
        }

        public DatabaseConfig build() {
            return new DatabaseConfig(this);
        }
    }
}
