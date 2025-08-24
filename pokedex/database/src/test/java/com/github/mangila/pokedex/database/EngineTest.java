package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.DatabaseName;
import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.HashKey;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

class EngineTest {

    @Test
    void abc() throws IOException, InterruptedException {
        var p = Paths.get("hej.wal");
        var e = new Engine(
                new FileManager(new WalFileManager(new DatabaseName("hej"))),
                new Cache(new LruCache<>(new LruCacheConfig(100)))
        );
        for (int i = 0; i < 21; i++) {
            e.appendAsync(
                    new HashKey("hash"),
                    new Field("hejsan" + i),
                    new Value(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0})
            );
        }
        Thread.sleep(5000);
    }

}