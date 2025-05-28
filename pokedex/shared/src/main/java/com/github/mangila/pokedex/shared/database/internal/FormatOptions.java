package com.github.mangila.pokedex.shared.database.internal;

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

    public static final byte[] POKEMON_MAGIC_NUMBER = "Pok3mon".getBytes();
    public static final byte[] VERSION = new byte[]{1};
    public static final int MAGIC_NUMBER_SIZE = POKEMON_MAGIC_NUMBER.length;
    public static final int VERSION_SIZE = VERSION.length;
    public static final int RECORD_COUNT_SIZE = 4;
    public static final int OFFSET_SIZE = 8;
    public static final int INDEX_OFFSET_SIZE = 8;
    public static final int DATA_OFFSET_SIZE = 8;
    public static final int HEADER_SIZE = MAGIC_NUMBER_SIZE + VERSION_SIZE + RECORD_COUNT_SIZE + INDEX_OFFSET_SIZE + DATA_OFFSET_SIZE;

}
