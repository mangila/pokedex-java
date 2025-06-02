package com.github.mangila.pokedex.shared;

import com.github.mangila.pokedex.shared.database.DatabaseConfig;
import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.model.Pokemon;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PokemonDatabaseTest {

    private static PokemonDatabase database;

    @BeforeAll
    static void beforeAll() {
        PokemonDatabase.configure(
                new DatabaseConfig(
                        new DatabaseName("pokedex"), 1));
        database = PokemonDatabase.getInstance();
        database.get().init();
    }

    @AfterAll
    static void afterAll() {
        database.get().deleteFile();
    }

    @AfterEach
    void afterEach() {
        database.get().truncate();
        assertThat(database.get().isEmpty()).isTrue();
    }

    @Test
    void ab() {
        var db = database.get();
        db.put("ivysaur", new Pokemon(22, "ivysaur"));
        db.put("venosaur", new Pokemon(33, "venosaur"));
        db.put("charizard", new Pokemon(77, "charizard"));
        var l = db.get("venosaur");
    }

    @Test
    void abc() {
        var db = database.get();
        db.put("asdf", new Pokemon(222, "asdf"));
        db.put("faf", new Pokemon(200, "faf"));
    }

}