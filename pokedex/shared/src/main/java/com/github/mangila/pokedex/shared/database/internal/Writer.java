package com.github.mangila.pokedex.shared.database.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static com.github.mangila.pokedex.shared.database.internal.Storage.*;

public class Writer {

    private final File file;

    public Writer(File file) {
        this.file = file;
    }

    // Try Pattern - Fail Safe
    public void tryCreateNewFile() throws IOException {
        if (!file.exists()) {
            boolean isNewFile = file.createNewFile();
            if (isNewFile) {
                init(file);
            }
        }
    }

    /**
     * Write File Headers with initial values
     */
    public static void init(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel()) {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, HEADER_SIZE);
            buffer.put(POKEMON_MAGIC_NUMBER);
            buffer.put(VERSION);
            buffer.putInt(0);
            buffer.putLong(INDEX_OFFSET_SIZE);
            buffer.putLong(DATA_OFFSET_SIZE);
            buffer.force();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
