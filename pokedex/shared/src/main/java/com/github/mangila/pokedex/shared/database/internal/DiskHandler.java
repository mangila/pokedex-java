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
            return instanceCreator.get();
        }
    }

    public void put(String key, V value) {
        int offset = writer.put(key, value)
                .join();
    }

    public void init() throws IOException {
        fileHandler.init();
        reader.init();
        writer.init();
    }

    public void deleteFile() throws IOException {
        reader.shutdown();
        writer.shutdown();
        fileHandler.deleteFile();
    }

    public void truncate() throws IOException, InterruptedException {
        fileHandler.truncate();
    }
}