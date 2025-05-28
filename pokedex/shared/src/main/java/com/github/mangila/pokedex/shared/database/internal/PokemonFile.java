package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32C;

import static com.github.mangila.pokedex.shared.database.internal.FormatOptions.*;

public class PokemonFile {

    private static final Logger log = LoggerFactory.getLogger(PokemonFile.class);

    private final String fileName;
    private final Map<String, Long> keyOffset = new ConcurrentHashMap<>();
    private final AtomicLong pokemonCount = new AtomicLong(0);
    private final FileChannel writeChannel;
    private final FileChannel readChannel;

    public PokemonFile(String fileName) {
        try {
            this.fileName = fileName;
            var path = Paths.get(fileName);
            var fileExists = Files.exists(path);
            this.writeChannel = FileChannel.open(
                    path,
                    fileExists ? FileOptions.OPEN_EXISTING_WRITE_OPTIONS : FileOptions.CREATE_NEW_WRITE_OPTIONS
            );
            init(fileExists);
            this.readChannel = FileChannel.open(
                    path,
                    FileOptions.READ_OPTIONS
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getKeyOffset(String key) {
        return keyOffset.get(key);
    }

    public void putKeyOffset(String key, Long offset) {
        keyOffset.put(key, offset);
        pokemonCount.incrementAndGet();
    }

    public long getPokemonCount() {
        return pokemonCount.get();
    }

    public Long write(String key, Pokemon pokemon) {
        var crc32c = new CRC32C();
        if (keyOffset.containsKey(key)) {
            // update
        } else {
            // new insert
        }
        return -1L;
    }

    public Pokemon read(String key) {
        if (!keyOffset.containsKey(key)) {
            return null;
        } else {
            var crc32c = new CRC32C();
            return new Pokemon(1, "bulba");
        }
    }

    private void init(boolean fileExists) throws IOException {
        if (fileExists) {
            log.info("{} - exists, loading indexes", fileName);
            loadOffsets();
        } else {
            log.info("{} - does not exist, creating new file", fileName);
            newOffsets();
        }
    }

    /**
     * <summary>
     * Load indexes from file
     * </summary>
     */
    private void loadOffsets() {

    }


    /**
     * <summary>
     * Write File Headers with initial values
     * </summary>
     */
    private void newOffsets() throws IOException {
        MappedByteBuffer buffer = writeChannel.map(
                FileChannel.MapMode.READ_WRITE,
                0,
                HEADER_SIZE);
        buffer.put(POKEMON_MAGIC_NUMBER);
        buffer.put(VERSION);
        buffer.putInt(0);
        buffer.putLong(INDEX_OFFSET_SIZE);
        buffer.putLong(DATA_OFFSET_SIZE);
        buffer.force();
    }
}
