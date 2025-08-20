package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;

/**
 * Handles operations with a database file in terms of reading, writing,
 * opening, closing, truncating, and deleting its contents. This class
 * provides standardized methods to interact with file channels.
 * <br>
 * It operates within the context of the {@code DatabaseFile} instance
 * which encapsulates the actual file path and channels for reading and
 * writing operations.
 */
public record DatabaseFileHandler(DatabaseFile databaseFile) {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFileHandler.class);

    private static final Set<StandardOpenOption> FILE_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.SYNC);

    public void init() throws IOException {
        LOGGER.debug("Initializing file {}", databaseFile.getPath());
        create();
        openChannel();
    }

    public FileChannel channel() {
        Ensure.isTrue(databaseFile.isOpen(), "File is not open");
        return databaseFile.getChannel();
    }

    public void openChannel() throws IOException {
        if (!databaseFile.isOpen()) {
            databaseFile.setChannel(
                    FileChannel.open(databaseFile.getPath(), FILE_OPTIONS)
            );
        } else {
            LOGGER.warn("Channel already open for {}", databaseFile.getPath());
        }
    }

    private void closeChannel() throws IOException {
        if (databaseFile.isOpen()) {
            databaseFile.getChannel().close();
        } else {
            LOGGER.warn("Channel already closed for {}", databaseFile.getPath());
        }
    }

    public void create() throws IOException {
        if (Files.exists(databaseFile.getPath())) {
            LOGGER.info("File {} already exists", databaseFile.getPath());
        } else {
            LOGGER.info("Creating file {}", databaseFile.getPath());
            Files.createFile(databaseFile.getPath());
        }
    }

    public void truncate() throws IOException {
        LOGGER.debug("Truncating file {}", databaseFile.getPath());
        if (databaseFile.isOpen()) {
            channel().truncate(0);
            channel().position(0);
        } else {
            throw new IOException("Cannot truncate file %s".formatted(databaseFile.getPath()));
        }
    }

    public void deleteFile() throws IOException {
        truncate();
        closeChannel();
        Files.delete(databaseFile.getPath());
    }
}
