package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    private final File file;
    private final Reader reader;
    private final Writer writer;
    private final Map<String, Long> keyIndex = new HashMap<>();

    private long recordCount = 0;

    public Storage(String fileName) {
        this.file = new File(fileName);
        this.reader = new Reader(file);
        this.writer = new Writer(file);
    }

    public Pokemon get(String key) {
        return null;
    }

    public void put(String key, Pokemon pokemon) {

    }

    public int size() {
        return (int) recordCount;
    }

    public boolean containsKey(String key) {
        return keyIndex.containsKey(key);
    }

    public void init() throws IOException {
        writer.tryCreateNewFile();
    }
}