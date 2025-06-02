package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.database.DatabaseObject;
import com.github.mangila.pokedex.shared.database.internal.file.FileHandler;
import com.github.mangila.pokedex.shared.database.internal.read.Reader;
import com.github.mangila.pokedex.shared.database.internal.write.Writer;
import com.github.mangila.pokedex.shared.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Supplier;

public class DiskHandler<V extends DatabaseObject<V>> {

    private static final Logger log = LoggerFactory.getLogger(DiskHandler.class);

    private final Supplier<V> instanceCreator;
    private final FileHandler<V> fileHandler;
    private final Reader<V> reader;
    private final Writer<V> writer;

    public DiskHandler(DatabaseName databaseName,
                       Supplier<V> instanceCreator) {
        this.instanceCreator = instanceCreator;
        this.fileHandler = new FileHandler<>(databaseName);
        this.reader = new Reader<>(fileHandler);
        this.writer = new Writer<>(fileHandler);
    }

    public V get(String key) {
        var bytes = reader.get(key)
                .exceptionally(throwable -> {
                    log.error("ERR", throwable);
                    return ArrayUtils.EMPTY_BYTE_ARRAY;
                })
                .join();
        if (ArrayUtils.isEmptyOrNull(bytes)) {
            return instanceCreator.get();
        }
        try {
            return instanceCreator.get().deserialize(bytes);
        } catch (IOException e) {
            log.error("ERR", e);
            return instanceCreator.get();
        }
    }

    public void put(String key, V value) {
        var result = writer.put(key, value)
                .join();
        if (result == 1) {
            log.warn("Record {} written", key);
        } else {
            log.debug("Record {} not written", key);
        }
    }

    public void init() throws IOException {
        fileHandler.init();
        reader.init();
        writer.init();
    }

    public void deleteFiles() throws IOException {
        reader.shutdown();
        writer.shutdown();
        fileHandler.deleteFiles();
    }

    public void truncateFiles() throws IOException {
        fileHandler.truncateFiles();
    }

    public boolean isEmpty() {
        return fileHandler.isEmpty();
    }
}