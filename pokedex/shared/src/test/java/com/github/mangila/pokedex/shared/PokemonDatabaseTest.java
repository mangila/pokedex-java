package com.github.mangila.pokedex.shared;

import com.github.mangila.pokedex.shared.database.DatabaseConfig;
import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.model.Pokemon;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
        database.get().deleteDatabase();
    }

    @AfterEach
    void afterEach() {
        database.get().truncateDatabase();
        assertThat(database.get().isEmpty())
                .isTrue();
    }

    @Test
    @Order(1)
    @DisplayName("Should insert new records")
    void shouldInsertNewRecords() {
        var db = database.get();
        db.put("ivysaur", new Pokemon(22, "ivysaur"));
        db.put("venosaur", new Pokemon(33, "venosaur"));
        db.put("charizard", new Pokemon(77, "charizard"));
        assertThat(db.get("venosaur"))
                .isNotEmpty()
                .get()
                .extracting(Pokemon::id, Pokemon::name)
                .contains(33, "venosaur");
    }

    @Test
    @Order(2)
    @DisplayName("Should insert new records after truncate")
    void shouldInsertNewRecordsAfterTruncate() {
        var db = database.get();
        db.put("mewtwo", new Pokemon(151, "mewtwo"));
        db.put("mew", new Pokemon(152, "mew"));
        assertThat(db.get("mewtwo"))
                .isNotEmpty()
                .get()
                .extracting(Pokemon::id, Pokemon::name)
                .contains(151, "mewtwo");
    }

}