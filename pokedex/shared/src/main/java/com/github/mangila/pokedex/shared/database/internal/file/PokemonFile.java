package com.github.mangila.pokedex.shared.database.internal.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PokemonFile {

    private static final Logger log = LoggerFactory.getLogger(PokemonFile.class);

    private final Path path;
    private FileChannel writeChannel;
    private FileChannel readChannel;

    public PokemonFile(PokemonFileName pokemonFileName) {
        try {
            this.path = Paths.get(pokemonFileName.value());
            if (!exists()) {
                log.info("Creating new file {}", path.getFileName());
                Files.createFile(path);
            }
        } catch (IOException e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    public FileChannel getReadChannel() throws IOException {
        if (readChannel == null || !readChannel.isOpen()) {
            log.debug("Opening read channel for {}", path.getFileName());
            this.readChannel = FileChannel.open(
                    path,
                    FileOptions.READ_OPTIONS
            );
        }
        return readChannel;
    }

    public FileChannel getWriteChannel() throws IOException {
        if (writeChannel == null || !writeChannel.isOpen()) {
            log.debug("Opening write channel for {}", path.getFileName());
            this.writeChannel = FileChannel.open(
                    path,
                    FileOptions.WRITE_OPTIONS
            );
        }
        return writeChannel;
    }

    public Path getPath() {
        return path;
    }

    public boolean exists() {
        return Files.exists(path);
    }

    public boolean isEmpty() {
        return exists() && path.toFile().length() == 0;
    }
}
