package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.Buffer;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFileHeader;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public record DatabaseFileModification(DatabaseFile databaseFile) {

    public void createIfNotExists() throws IOException {
        Path path = databaseFile.path();
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    public void truncate() throws IOException {
        SeekableByteChannel channel = Files.newByteChannel(databaseFile.path(),
                StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.SYNC);
        channel.truncate(0);
        DatabaseFileHeader emptyHeader = DatabaseFileHeader.EMPTY;
        Buffer buffer = emptyHeader.toBuffer(true);
        channel.write(buffer.value());
        channel.close();
    }

    public void delete() throws IOException {
        Files.delete(databaseFile.path());
    }

}
