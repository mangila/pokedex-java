package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.database.internal.file.PokemonFile;
import com.github.mangila.pokedex.shared.database.internal.file.PokemonFileHandler;
import com.github.mangila.pokedex.shared.database.internal.file.PokemonFileName;
import com.github.mangila.pokedex.shared.database.internal.read.Reader;
import com.github.mangila.pokedex.shared.database.internal.write.Writer;
import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskHandler {

    private static final Logger log = LoggerFactory.getLogger(DiskHandler.class);

    private final PokemonFileHandler pokemonFileHandler;
    private final Reader reader;
    private final Writer writer;

    public DiskHandler(PokemonFileName pokemonFileName) {
        var pokemonFile = new PokemonFile(pokemonFileName);
        this.pokemonFileHandler = new PokemonFileHandler(pokemonFile);
        this.reader = new Reader(pokemonFileHandler);
        this.writer = new Writer(pokemonFileHandler);
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
        pokemonFileHandler.deleteFile();
    }
}