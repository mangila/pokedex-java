package com.github.mangila.pokedex.shared.cache;

import com.github.mangila.pokedex.shared.cache.ttl.JsonResponseTtlCache;
import com.github.mangila.pokedex.shared.cache.ttl.JsonResponseTtlCacheConfig;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class JsonResponseTtlCacheTest {

    @Test
    void shouldPutKeyAndRemoveAfterTtl() {
        var config = new JsonResponseTtlCacheConfig(
                Duration.ofSeconds(3),
                0,
                3,
                SECONDS
        );
        var cache = new JsonResponseTtlCache(config);
        cache.startEvictionThread();
        cache.put("key", new JsonResponse(null, null, null));
        assertThat(cache.hasKey("key")).isTrue();
        await()
                .atMost(10, SECONDS)
                .until(() -> !cache.hasKey("key"));
    }

    @Test
    void shouldShutdownEvictionThreadAndNotRemoveKey() throws InterruptedException {
        var config = new JsonResponseTtlCacheConfig(
                Duration.ofSeconds(3),
                0,
                3,
                SECONDS
        );
        var cache = new JsonResponseTtlCache(config);
        cache.startEvictionThread();
        cache.put("key", new JsonResponse(null, null, null));
        assertThat(cache.hasKey("key")).isTrue();
        cache.shutdownEvictionThread();
        SECONDS.sleep(5);
        assertThat(cache.isShutdownAndTerminated()).isTrue();
        assertThat(cache.hasKey("key")).isTrue();
    }
}