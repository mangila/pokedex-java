package com.github.mangila.pokedex.shared.tls;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class TlsConnectionPoolTest {

    @Test
    void borrow() throws InterruptedException {
        var pool = new TlsConnectionPool("pokeapi.co", 443).connect();
        var threads = Executors.newFixedThreadPool(10, Thread.ofVirtual().factory());

        for (int i = 0; i < 10; i++) {
            threads.submit(() -> {
                try {
                    var latch = new CountDownLatch(1);
                    var con = pool.borrow(Duration.ofSeconds(5));
                    if (con.isEmpty()) {
                        latch.countDown();
                        return;
                    }
                    latch.await(4, TimeUnit.SECONDS);
                    pool.giveBack(con.get());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        while (true) {

        }

    }
}