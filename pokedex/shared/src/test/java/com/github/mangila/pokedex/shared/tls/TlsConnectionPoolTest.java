package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;
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

    private static final int MAX_CONNECTIONS = 2;
    private static final String HOST = "httpbin.org";
    private static final int PORT = 443;
    private static final TlsConnectionPoolConfig.HealthCheckConfig HEALTH_CHECK_CONFIG = new TlsConnectionPoolConfig.HealthCheckConfig(
            0,
            5,
            TimeUnit.SECONDS
    );
    private static final TlsConnectionPoolConfig CONFIG = new TlsConnectionPoolConfig(HOST, PORT, MAX_CONNECTIONS, HEALTH_CHECK_CONFIG);
    private static final TlsConnectionPool GENERAL_PURPOSE_TESTING_POOL = new TlsConnectionPool(CONFIG);
    private static final ExecutorService GENERAL_PURPOSE_TESTING_EXECUTOR = Executors.newFixedThreadPool(2, Thread.ofVirtual().factory());

    @BeforeAll
    static void init() {
        GENERAL_PURPOSE_TESTING_POOL.init();
    }

    @AfterAll
    static void shutdown() {
        GENERAL_PURPOSE_TESTING_POOL.shutdownConnectionPool();
    }

    @Test
    void shouldBorrowAndReturnConnection() {
        GENERAL_PURPOSE_TESTING_EXECUTOR.submit(() -> {
            try {
                var tlsConnection = GENERAL_PURPOSE_TESTING_POOL.borrow();
                TimeUnit.SECONDS.sleep(1);
                GENERAL_PURPOSE_TESTING_POOL.returnConnection(tlsConnection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        GENERAL_PURPOSE_TESTING_EXECUTOR.submit(() -> {
            try {
                var tlsConnection = GENERAL_PURPOSE_TESTING_POOL.borrow();
                TimeUnit.SECONDS.sleep(1);
                GENERAL_PURPOSE_TESTING_POOL.returnConnection(tlsConnection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> GENERAL_PURPOSE_TESTING_POOL.poolSize() == 2);
    }

    @Test
    void shouldThrowExceptionWhenQueueIsFull() {
        assertThatThrownBy(GENERAL_PURPOSE_TESTING_POOL::addNewConnection)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Queue full");
    }

    @Test
    void shouldThrowExceptionWhenPoolIsShutdown() {
        var pool = new TlsConnectionPool(new TlsConnectionPoolConfig(HOST, PORT, 1, HEALTH_CHECK_CONFIG));
        pool.init();
        pool.shutdownConnectionPool();
        assertThatThrownBy(pool::borrow)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Connection pool is not initialized");
    }
}