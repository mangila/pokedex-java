package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.MemTable;
import com.github.mangila.pokedex.database.model.WalFile;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Supplier;

public class Database<T extends DatabaseObject<T>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    private final Engine engine;
    private final Supplier<T> instanceCreator;

    public Database(DatabaseConfig config,
                    Supplier<T> instanceCreator) {
        var p = Path.of("hej.wal");
        this.engine = new Engine(
                new FileManager(new WalFileManager(new WalFile(p), new MemTable(new ConcurrentSkipListMap<>(Comparator.comparing(Key::value))))),
                new Cache(new LruCache<>(config.lruCacheConfig()))
        );
        this.instanceCreator = instanceCreator;
    }
}
