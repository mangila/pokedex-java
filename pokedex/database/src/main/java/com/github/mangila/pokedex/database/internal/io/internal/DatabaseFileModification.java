package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public record DatabaseFileModification(DatabaseFile databaseFile) {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFileModification.class);

    public void createIfNotExists() throws IOException {
        Path path = databaseFile.path();
        if (!Files.exists(path)) {
            LOGGER.info("Creating file {}", path);
            Files.createFile(path);
        } else {
            LOGGER.info("File {} already exists", path);
        }
    }

    public void truncate() throws IOException {
        LOGGER.debug("Truncating file {}", databaseFile.path());
        SeekableByteChannel channel = Files.newByteChannel(databaseFile.path(),
                StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.SYNC);
        channel.truncate(0);
        channel.close();
    }

    public void delete() throws IOException {
        LOGGER.debug("Deleting file {}", databaseFile.path());
        Files.delete(databaseFile.path());
    }

}
