package com.github.mangila.pokedex.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class DatabaseTest {

    private record TestObject() implements DatabaseObject<TestObject> {

        @Override
        public byte[] serialize() throws IOException {
            return new byte[0];
        }

        @Override
        public TestObject deserialize(byte[] data) throws IOException {
            return null;
        }

        @Override
        public double schemaVersion() {
            return 0;
        }
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void init() {
    }

    @Test
    void close() {
    }

    @Test
    void truncateAsync() {
    }

    @Test
    void deleteAsync() {
    }

    @Test
    void size() {
    }

    @Test
    void putAsync() {
    }

    @Test
    void getAsync() {
    }
}