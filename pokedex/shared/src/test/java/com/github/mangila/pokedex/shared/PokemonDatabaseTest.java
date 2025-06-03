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
        db.putAsync("ivysaur", new Pokemon(22, "ivysaur")).join();
        db.putAsync("venosaur", new Pokemon(33, "venosaur")).join();
        db.putAsync("charizard", new Pokemon(77, "charizard")).join();
        assertThat(db.getAsync("venosaur").join())
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
        db.putAsync("mewtwo", new Pokemon(151, "mewtwo")).join();
        db.putAsync("mew", new Pokemon(152, "mew")).join();
        assertThat(db.getAsync("mewtwo").join())
                .isNotEmpty()
                .get()
                .extracting(Pokemon::id, Pokemon::name)
                .contains(151, "mewtwo");
    }

}