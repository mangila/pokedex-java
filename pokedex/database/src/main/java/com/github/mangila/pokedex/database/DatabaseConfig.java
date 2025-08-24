package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.DatabaseName;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.util.Ensure;

public record DatabaseConfig(
        DatabaseName databaseName,
        LruCacheConfig lruCacheConfig) {

    private DatabaseConfig(Builder builder) {
        this(builder.databaseName, builder.lruCacheConfig);
        Ensure.notNull(databaseName, "database name must not be null");
        Ensure.notNull(lruCacheConfig, "lru cache config must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DatabaseName databaseName;
        private LruCacheConfig lruCacheConfig = new LruCacheConfig(100);

        private Builder() {
        }

        public Builder databaseName(DatabaseName databaseName) {
            this.databaseName = databaseName;
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
