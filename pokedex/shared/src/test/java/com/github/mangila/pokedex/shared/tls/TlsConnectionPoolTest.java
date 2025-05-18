package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.testutil.TestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class TlsConnectionPoolTest {

    private static final TlsConnectionPool TEST_CONNECTION_POOL = TestUtil.createNewTestingTlsConnectionPool(2);

    @BeforeAll
    static void init() {
        TEST_CONNECTION_POOL.init();
    }

    @AfterAll
    static void shutdown() {
        TEST_CONNECTION_POOL.shutdownConnectionPool();
    }

    @Test
    void shouldBorrowAndReturnConnection() {
        var executor = VirtualThreadConfig.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
                var tlsConnection = TEST_CONNECTION_POOL.borrow();
                TimeUnit.SECONDS.sleep(1);
                TEST_CONNECTION_POOL.returnConnection(tlsConnection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executor.submit(() -> {
            try {
                var tlsConnection = TEST_CONNECTION_POOL.borrow();
                TimeUnit.SECONDS.sleep(1);
                TEST_CONNECTION_POOL.returnConnection(tlsConnection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> TEST_CONNECTION_POOL.remainingCapacity() == 2);
    }

    @Test
    void shouldReturnEmptyOptionalWhenBorrowTimeout() {
        var executor = VirtualThreadConfig.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
                var tlsConnection = TEST_CONNECTION_POOL.borrow();
                TimeUnit.SECONDS.sleep(10);
                TEST_CONNECTION_POOL.returnConnection(tlsConnection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executor.submit(() -> {
            try {
                var tlsConnection = TEST_CONNECTION_POOL.borrow();
                TimeUnit.SECONDS.sleep(10);
                TEST_CONNECTION_POOL.returnConnection(tlsConnection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        await()
                .pollDelay(Duration.ofSeconds(1))
                .until(() -> TEST_CONNECTION_POOL.borrow(Duration.ofSeconds(1))
                        .isEmpty());
    }

    @Test
    void shouldBorrowAndHaveNoRemainingCapacity() throws InterruptedException {
        var tlsConnection1 = TEST_CONNECTION_POOL.borrow();
        var tlsConnection2 = TEST_CONNECTION_POOL.borrow();
        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> TEST_CONNECTION_POOL.remainingCapacity() == 2);
        assertThat(TEST_CONNECTION_POOL.poolCapacity())
                .isEqualTo(2);
        TEST_CONNECTION_POOL.returnConnection(tlsConnection1);
        TEST_CONNECTION_POOL.returnConnection(tlsConnection2);
        assertThat(TEST_CONNECTION_POOL.remainingCapacity())
                .isEqualTo(0);
        assertThat(TEST_CONNECTION_POOL.poolCapacity())
                .isEqualTo(2);
    }

    @Test
    void shouldReconnectIfUnhealthyBorrow() throws InterruptedException {
        var pool = TestUtil.createNewTestingTlsConnectionPool(1);
        pool.init();
        var tlsConnection = pool.borrow();
        tlsConnection.disconnect();
        assertThat(tlsConnection.isConnected())
                .isFalse();
        pool.returnConnection(tlsConnection);
        var newConnection = pool.borrow();
        assertThat(tlsConnection)
                .extracting(PooledTlsConnection::id, PooledTlsConnection::isConnected)
                .contains(newConnection.id(), Boolean.TRUE);
    }

    @Test
    void shouldReconnectIfUnhealthyHealthProbe() throws InterruptedException {
        var pool = TestUtil.createNewTestingTlsConnectionPool(1);
        pool.init();
        var tlsConnection = pool.borrow();
        tlsConnection.disconnect();
        assertThat(tlsConnection.isConnected())
                .isFalse();
        pool.returnConnection(tlsConnection);
        await()
                .atMost(Duration.ofSeconds(30))
                .until(tlsConnection::isConnected);
    }

    @Test
    void shouldThrowExceptionWhenQueueIsFull() {
        assertThatThrownBy(TEST_CONNECTION_POOL::addNewConnection)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Queue full");
    }

    @Test
    void shouldThrowExceptionWhenPoolIsShutdown() {
        var pool = TestUtil.createNewTestingTlsConnectionPool(1);
        pool.init();
        pool.shutdownConnectionPool();
        assertThatThrownBy(pool::borrow)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Connection pool is not initialized");
    }
}