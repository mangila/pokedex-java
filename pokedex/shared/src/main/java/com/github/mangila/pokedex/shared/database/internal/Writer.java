package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.model.Pokemon;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static com.github.mangila.pokedex.shared.database.internal.Storage.*;

public class Writer {

    private final PokemonFile pokemonFile;

    public Writer(PokemonFile pokemonFile) {
        this.pokemonFile = pokemonFile;
    }

    public long newRecord(String key, Pokemon pokemon) {
        return -1L;
    }

    /**
     * <summary>
     * Write File Headers with initial values
     * </summary>
     */
    public void init() {
        try (RandomAccessFile raf = new RandomAccessFile(pokemonFile.getIoFile(), "rw");
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

    /**
     * <summary>
     * Load indexes from file
     * </summary>
     */
    public void load() {

    }
}
