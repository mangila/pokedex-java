package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.config.DatabaseConfig;
import com.github.mangila.pokedex.database.model.DatabaseName;
import com.github.mangila.pokedex.shared.Config;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class DefaultEngineTest {

    @Test
    void abc() throws InterruptedException {
        QueueService.getInstance().createNewQueue(Config.DATABASE_WAL_FLUSH_BUFFER_QUEUE);
        var e = new DefaultEngine(
                new FileManager(DatabaseConfig.builder()
                        .databaseName(new DatabaseName("test"))
                        .build()),
                new Cache(new LruCache<>(new LruCacheConfig(100)))
        );
        for (int i = 0; i < 1_000_000; i++) {
            e.putAsync(
                    "hash",
                    "hej" + i,
                    new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0}
            );
        }
        Thread.sleep(5000);
    }

}