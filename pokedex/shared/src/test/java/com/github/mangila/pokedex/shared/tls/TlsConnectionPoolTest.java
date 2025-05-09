package com.github.mangila.pokedex.shared.tls;

import org.junit.jupiter.api.Test;

import java.time.Duration;

class TlsConnectionPoolTest {

    @Test
    void borrow() throws InterruptedException {
        var pool = new TlsConnectionPool("pokeapi.co", 443)
                .connect();
        var con = pool.borrow(Duration.ofSeconds(10));
        var con1 = pool.borrow(Duration.ofSeconds(10));
        var con2 = pool.borrow(Duration.ofSeconds(10));

        pool.put(con.get());
        pool.put(con1.get());
        pool.put(con2.get());

    }
}