package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.database.internal.file.FileHandler;
import com.github.mangila.pokedex.shared.database.internal.read.Reader;
import com.github.mangila.pokedex.shared.database.internal.write.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DiskHandler {

    private static final Logger log = LoggerFactory.getLogger(DiskHandler.class);

    private final FileHandler fileHandler;
    private final Reader reader;
    private final Writer writer;

    public DiskHandler(DatabaseName databaseName) {
        this.fileHandler = new FileHandler(databaseName);
        this.reader = new Reader(fileHandler);
        this.writer = new Writer(fileHandler);
    }

    public CompletableFuture<byte[]> get(String key) {
        return reader.get(key);
    }

    public CompletableFuture<Integer> put(String key, byte[] value) {
        return writer.put(key, value);
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