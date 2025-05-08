package com.github.mangila.pokedex.shared.cache;

import com.github.mangila.pokedex.shared.https.model.Response;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ResponseTtlCacheTest {

    @Test
    void testEvict() {
        var config = new ResponseTtlCacheConfig(
                Duration.ofSeconds(3),
                0,
                3,
                SECONDS
        );
        var cache = new ResponseTtlCache(config);
        cache.put("key", new Response(null, null, null));
        assertThat(cache.hasKey("key")).isTrue();
        await()
                .atMost(5, SECONDS)
                .until(() -> !cache.hasKey("key"));
    }

    @Test
    void testShutdownEvictionThread() {
        var config = new ResponseTtlCacheConfig(
                Duration.ofSeconds(10),
                0,
                3,
                SECONDS
        );
        var cache = new ResponseTtlCache(config);
        cache.put("key", new Response(null, null, null));
        assertThat(cache.hasKey("key")).isTrue();
        cache.shutdownEvictionThread();
        await()
                .atMost(5, SECONDS)
                .until(cache::isShutdownAndTerminated);
        assertThat(cache.hasKey("key")).isTrue();
    }
}