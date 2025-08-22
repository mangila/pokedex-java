package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.MemTable;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.database.model.WalFile;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

class EngineTest {

    @Test
    void abc() throws IOException, InterruptedException {
        var p = Paths.get("hej.wal");
        var e = new Engine(
                new FileManager(new WalFileManager(new WalFile(p), new MemTable(new ConcurrentSkipListMap<>(Comparator.comparing(Key::value))))),
                new Cache(new LruCache<>(new LruCacheConfig(100)))
        );
        e.init();
        for (int i = 0; i < 150; i++) {
            e.putAsync(new Key("hejsan" + i), new Value(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0}));
        }
        Thread.sleep(100);
    }

}