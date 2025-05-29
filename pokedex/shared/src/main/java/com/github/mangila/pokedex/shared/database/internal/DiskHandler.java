package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.database.internal.read.Reader;
import com.github.mangila.pokedex.shared.database.internal.write.Writer;
import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskHandler {

    private static final Logger log = LoggerFactory.getLogger(DiskHandler.class);

    private final PokemonFile pokemonFile;
    private final Reader reader;
    private final Writer writer;

    public DiskHandler(String fileName) {
        this.pokemonFile = new PokemonFile(fileName);
        this.reader = new Reader(pokemonFile);
        this.writer = new Writer(pokemonFile);
    }

    public Pokemon get(String key) {
        return reader.get(key).join();
    }

    public void put(String key, Pokemon pokemon) {
        var offset = writer.newRecord(key, pokemon)
                .join();
    }

    public void deleteFile() {
        reader.shutdown();
        writer.shutdown();
        pokemonFile.deleteFile();
    }
}