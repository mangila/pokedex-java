package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.config.DatabaseConfig;
import com.github.mangila.pokedex.database.model.DatabaseName;
import com.github.mangila.pokedex.shared.Config;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDatabaseTest {

    static Database db;

    @BeforeAll
    static void beforeAll() {
        QueueService.getInstance().createNewBlockingQueue(Config.DATABASE_WAL_WRITE_QUEUE);
        db = new DefaultDatabase(DatabaseConfig.builder()
                .databaseName(new DatabaseName("test"))
                .build());
        db.open();
        assertThat(db.isOpen()).isTrue();
    }

    @AfterAll
    static void afterAll() {
        db.close();
        assertThat(db.isOpen()).isFalse();
    }

    @Test
    void readAndWrite() {
        var f = db.putAsync("key", "value", "abcåäö").join();
        var ff = f.future().join();
        String v = db.getString("key", "value");
        assertThat(v).isEqualTo("abcåäö");


        f = db.putAsync("key", "value", "hej").join();
        ff = f.future().join();
        v = db.getString("key", "value");
        assertThat(v).isEqualTo("hej");
    }

}