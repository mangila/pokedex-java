package com.github.mangila.pokedex.shared.database.internal;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PokemonFile {

    private final Map<String, Long> keyOffset = new ConcurrentHashMap<>();
    private final AtomicLong pokemonCount = new AtomicLong(0);
    private final File ioFile;

    public PokemonFile(String fileName) {
        this.ioFile = new File(fileName);
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

    /**
     * <summary>
     * Try Pattern - Fail Safe
     * </summary>
     */
    public boolean tryCreateNewFile() throws IOException {
        return ioFile.createNewFile();
    }

    public File getIoFile() {
        return ioFile;
    }
}
