package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.model.Pokemon;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * TODO WIP
 * <summary>
 * [HEADER]
 * - Magic Number ("Pok3mon" bytes)
 * - Version (n bytes)
 * - Record Count (4 bytes)
 * - Index Offset (8 bytes)
 * - Data Offset (8 bytes)
 * <p>
 * [DATA SECTION]
 * - Pokemon Record 1: Length (4 bytes) + Serialized Pokemon data
 * - Pokemon Record 2: Length (4 bytes) + Serialized Pokemon data
 * - ...
 * <p>
 * [INDEX SECTION]
 * - Entry 1: Key length (4 bytes) + Key bytes + Data offset (8 bytes)
 * - Entry 2: Key length (4 bytes) + Key bytes + Data offset (8 bytes)
 * - ...
 * </summary>
 */
public class PokemonFile {

    public static final byte[] POKEMON_MAGIC_NUMBER = "Pok3mon".getBytes();
    public static final byte[] VERSION = new byte[]{1};
    public static final int MAGIC_NUMBER_SIZE = POKEMON_MAGIC_NUMBER.length;
    public static final int VERSION_SIZE = VERSION.length;
    public static final int RECORD_COUNT_SIZE = 4;
    public static final int OFFSET_SIZE = 8;
    public static final int INDEX_OFFSET_SIZE = 8;
    public static final int DATA_OFFSET_SIZE = 8;
    public static final int HEADER_SIZE = MAGIC_NUMBER_SIZE + VERSION_SIZE + RECORD_COUNT_SIZE + INDEX_OFFSET_SIZE + DATA_OFFSET_SIZE;

    private static final Set<StandardOpenOption> CREATE_NEW_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE_NEW,
            StandardOpenOption.DSYNC);

    private static final Set<StandardOpenOption> OPEN_EXISTING_OPTIONS = EnumSet.of(
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.DSYNC);

    private final Map<String, Long> keyOffset = new ConcurrentHashMap<>();
    private final AtomicLong pokemonCount = new AtomicLong(0);
    private final FileChannel fileChannel;

    public PokemonFile(String fileName) {
        try {
            var path = Paths.get(fileName);
            var fileExists = Files.exists(path);
            this.fileChannel = FileChannel.open(
                    path,
                    fileExists ? OPEN_EXISTING_OPTIONS : CREATE_NEW_OPTIONS
            );
            init(fileExists);
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
            return new Pokemon(1, "bulba");
        }
    }

    private void init(boolean fileExists) throws IOException {
        if (fileExists) {
            loadOffsets();
        } else {
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
        MappedByteBuffer buffer = fileChannel.map(
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
