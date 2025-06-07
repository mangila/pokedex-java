package com.github.mangila.pokedex.shared;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.database.DatabaseConfig;
import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.database.internal.file.CompactThread;
import com.github.mangila.pokedex.shared.model.Pokemon;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PokemonDatabaseTest {

    private static PokemonDatabase database;

    @BeforeAll
    static void beforeAll() {
        PokemonDatabase.configure(new DatabaseConfig(
                new DatabaseName("test"),
                new LruCacheConfig(1),
                new DatabaseConfig.CompactThreadConfig(5, 10, TimeUnit.SECONDS),
                new DatabaseConfig.ReaderThreadConfig(3, 50),
                new DatabaseConfig.WriteThreadConfig(10)
        ));
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


    @Test
    void shouldCompact() {
        Logger logger = (Logger) LoggerFactory.getLogger(CompactThread.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        var db = database.get();
        db.putAsync("bulbasaur", new Pokemon(1, "bulbasaur")).join();
        db.putAsync("bulbasaur", new Pokemon(1, "bulbasaur")).join();
        db.putAsync("squritle", new Pokemon(2, "squritle")).join();
        db.putAsync("squritle", new Pokemon(2, "squritle")).join();
        db.putAsync("charmander", new Pokemon(3, "charmander")).join();
        db.putAsync("charmander", new Pokemon(3, "charmander")).join();
        var logs = listAppender.list;
        await()
                .atMost(Duration.ofMinutes(1))
                .until(() -> {
                    var value = db.getAsync("bulbasaur").join();
                    assertThat(value)
                            .isNotEmpty();
                    value = db.getAsync("squritle").join();
                    assertThat(value)
                            .isNotEmpty();
                    value = db.getAsync("charmander").join();
                    assertThat(value)
                            .isNotEmpty();
                    return logs.size() == 4;
                });

        var firstCompact = logs.get(1);
        var secondCompact = logs.get(3);
        assertThat(firstCompact.getLevel())
                .isEqualTo(Level.INFO);
        assertThat(firstCompact.getFormattedMessage())
                .contains("Old size: 175 bytes, new size: 94 bytes");
        assertThat(secondCompact.getLevel())
                .isEqualTo(Level.INFO);
        assertThat(secondCompact.getFormattedMessage())
                .contains("Old size: 94 bytes, new size: 94 bytes");
    }

}