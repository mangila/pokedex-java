package com.github.mangila.pokedex.shared.database.internal.file;

/**
 * TODO WIP
 * <summary>
 * [HEADER]
 * - Magic Number ("Pok3mon" bytes)
 * - Version (4 bytes)
 * - Record Count (4 bytes)
 * - Index Offset (8 bytes)
 * - Data Offset (8 bytes)
 * <p>
 * [DATA SECTION]
 * - Pokemon Record 1: Length (4 bytes) + Serialized Pokemon data + CRC32C
 * - Pokemon Record 2: Length (4 bytes) + Serialized Pokemon data + CRC32C
 * - ...
 * <p>
 * [INDEX SECTION]
 * - Entry 1: Key length (4 bytes) + Key bytes + Data offset (8 bytes)
 * - Entry 2: Key length (4 bytes) + Key bytes + Data offset (8 bytes)
 * - ...
 * </summary>
 */
public final class FormatOptions {

    public static final String POKEMON_MAGIC_NUMBER = "Pok3mon";
    public static final byte[] POKEMON_MAGIC_NUMBER_BYTES = POKEMON_MAGIC_NUMBER.getBytes();
    public static final int VERSION = 1;

    // Header sizes
    public static final int POKEMON_MAGIC_NUMBER_SIZE = POKEMON_MAGIC_NUMBER.length();
    public static final int VERSION_SIZE = 4;
    public static final int RECORD_COUNT_SIZE = 4;
    public static final int INDEX_OFFSET_SIZE = 8;
    public static final int DATA_OFFSET_SIZE = 8;
    public static final int HEADER_SIZE = POKEMON_MAGIC_NUMBER_SIZE +
            VERSION_SIZE +
            RECORD_COUNT_SIZE +
            INDEX_OFFSET_SIZE +
            DATA_OFFSET_SIZE;
}
