package com.github.mangila.pokedex.shared.tls.pool;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.testutil.TestUtil;
import com.github.mangila.pokedex.shared.tls.TlsConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class TlsConnectionPoolTest {

    private TlsConnectionPool pool;

    @BeforeEach
    void setUp() {
        pool = TestUtil.createNewTestingTlsConnectionPool(2);
        pool.init();
    }

    @Test
    void shouldBorrowAndReturnConnection() {
        // Given
        var executor = VirtualThreadConfig.newFixedThreadPool(2);

        // When
        executor.submit(() -> {
            try {
                var tlsConnection = pool.borrow();
                TimeUnit.SECONDS.sleep(1);
                pool.offer(tlsConnection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        executor.submit(() -> {
            try {
                var tlsConnection = pool.borrow();
                TimeUnit.SECONDS.sleep(1);
                pool.offer(tlsConnection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> pool.availablePermits() == 2);
    }

    @Test
    void shouldThrowExceptionWhenPoolNotInitialized() {
        // Given
        var uninitializedPool = TestUtil.createNewTestingTlsConnectionPool(1);

        // Then
        assertThatThrownBy(uninitializedPool::borrow)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Connection pool is not initialized");
    }

    @Test
    void shouldCreateNewConnection() {
        // When
        TlsConnection connection = pool.createNewConnection();

        // Then
        assertThat(connection).isNotNull();
    }
}