package com.github.mangila.pokedex.shared.cache;

import com.github.mangila.pokedex.shared.cache.ttl.TtlCache;
import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class TtlCacheTest {

    @Test
    void shouldPutKeyAndRemoveAfterTtl() {
        var config = new TtlCacheConfig(
                Duration.ofSeconds(3),
                0,
                3,
                SECONDS
        );
        var cache = new TtlCache(config);
        cache.startEvictionThread();
        cache.put("key", new JsonResponse(null, null, null));
        assertThat(cache.hasKey("key")).isTrue();
        await()
                .atMost(10, SECONDS)
                .until(() -> !cache.hasKey("key"));
    }

    @Test
    void shouldShutdownEvictionThreadAndNotRemoveKey() throws InterruptedException {
        var config = new TtlCacheConfig(
                Duration.ofSeconds(3),
                0,
                3,
                SECONDS
        );
        var cache = new TtlCache(config);
        cache.startEvictionThread();
        cache.put("key", new JsonResponse(null, null, null));
        assertThat(cache.hasKey("key")).isTrue();
        cache.shutdownEvictionThread();
        SECONDS.sleep(5);
        assertThat(cache.isShutdownAndTerminated()).isTrue();
        assertThat(cache.hasKey("key")).isTrue();
    }
}