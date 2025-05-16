package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.testutil.TestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class TlsConnectionPoolTest {

    private static final TlsConnectionPool TEST_POOL = TestUtil.createNewTestingTlsConnectionPool();
    private static final ExecutorService GENERAL_PURPOSE_TESTING_EXECUTOR = Executors.newFixedThreadPool(2, Thread.ofVirtual().factory());

    @BeforeAll
    static void init() {
        TEST_POOL.init();
    }

    @AfterAll
    static void shutdown() {
        TEST_POOL.shutdownConnectionPool();
    }

    @Test
    void shouldBorrowAndReturnConnection() {
        GENERAL_PURPOSE_TESTING_EXECUTOR.submit(() -> {
            try {
                var tlsConnection = TEST_POOL.borrow();
                TimeUnit.SECONDS.sleep(1);
                TEST_POOL.returnConnection(tlsConnection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        GENERAL_PURPOSE_TESTING_EXECUTOR.submit(() -> {
            try {
                var tlsConnection = TEST_POOL.borrow();
                TimeUnit.SECONDS.sleep(1);
                TEST_POOL.returnConnection(tlsConnection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> TEST_POOL.poolSize() == 2);
    }

    @Test
    void shouldThrowExceptionWhenQueueIsFull() {
        assertThatThrownBy(TEST_POOL::addNewConnection)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Queue full");
    }

    @Test
    void shouldThrowExceptionWhenPoolIsShutdown() {
        var pool = TestUtil.createNewTestingTlsConnectionPool();
        pool.init();
        pool.shutdownConnectionPool();
        assertThatThrownBy(pool::borrow)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Connection pool is not initialized");
    }
}