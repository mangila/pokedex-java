package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public sealed class AbstractFileHandler permits DataFileHandler, IndexFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileHandler.class);
    private final String fileName;
    private final DatabaseFileAccess fileAccess;
    private final DatabaseFileModification fileModification;

    public AbstractFileHandler(DatabaseFile databaseFile) {
        this.fileName = databaseFile.path().getFileName().toString();
        this.fileAccess = new DatabaseFileAccess(new DatabaseFileChannelHandler(databaseFile));
        this.fileModification = new DatabaseFileModification(databaseFile);
    }

    public void init() throws IOException {
        fileModification.createIfNotExists();
        fileAccess.channelHandler().open();
        if (fileAccess().isEmpty()) {
            fileAccess().writeHeader(DatabaseFileHeader.EMPTY);
        }
    }

    public void truncate() throws IOException {
        LOGGER.info("Truncating database file {}", fileName());
        fileModification().truncate();
    }

    public void delete() throws IOException {
        LOGGER.info("Deleting database file {}", fileName());
        fileAccess().channelHandler().close();
        fileModification().delete();
    }

    public DatabaseFileAccess fileAccess() {
        return fileAccess;
    }

    public DatabaseFileModification fileModification() {
        return fileModification;
    }

    public String fileName() {
        return fileName;
    }
}
