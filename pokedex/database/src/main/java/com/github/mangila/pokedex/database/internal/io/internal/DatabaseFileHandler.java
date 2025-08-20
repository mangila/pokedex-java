package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DatabaseFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFileHandler.class);
    private final DatabaseFileAccess fileAccess;
    private final DatabaseFileModification fileModification;

    public DatabaseFileHandler(DatabaseFile databaseFile) {
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

    public DatabaseFileAccess fileAccess() {
        return fileAccess;
    }

    public DatabaseFileModification fileModification() {
        return fileModification;
    }
}
