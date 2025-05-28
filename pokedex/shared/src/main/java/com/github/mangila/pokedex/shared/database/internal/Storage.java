package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
public class Storage {

    public static final byte[] POKEMON_MAGIC_NUMBER = "Pok3mon".getBytes();
    public static final byte[] VERSION = new byte[]{1};
    public static final int MAGIC_NUMBER_SIZE = POKEMON_MAGIC_NUMBER.length;
    public static final int VERSION_SIZE = VERSION.length;
    public static final int RECORD_COUNT_SIZE = 4;
    public static final int OFFSET_SIZE = 8;
    public static final int INDEX_OFFSET_SIZE = 8;
    public static final int DATA_OFFSET_SIZE = 8;
    public static final int HEADER_SIZE = MAGIC_NUMBER_SIZE + VERSION_SIZE + RECORD_COUNT_SIZE + INDEX_OFFSET_SIZE + DATA_OFFSET_SIZE;

    private static final Logger log = LoggerFactory.getLogger(Storage.class);

    private final PokemonFile file;
    private final Reader reader;
    private final Writer writer;

    public Storage(String fileName) {
        this.file = new PokemonFile(fileName);
        this.reader = new Reader(file);
        this.writer = new Writer(file);
    }

    public Pokemon get(String key) {
        var offset = file.getKeyOffset(key);
        var pokemon = reader.get(offset);
        log.trace("{} -> {}", key, pokemon);
        return pokemon;
    }

    public void put(String key, Pokemon pokemon) {
        var offset = writer.newRecord(key, pokemon);
        file.putKeyOffset(key, offset);
        log.trace("{} -> {}", key, offset);
    }

    public void init() throws IOException {
        var isCreated = file.tryCreateNewFile();
        if (isCreated) {
            log.info("Created new file {}", file.getIoFile().getName());
            writer.init();
        } else {
            log.info("File {} already exists", file.getIoFile().getName());
            writer.load();
        }
    }
}