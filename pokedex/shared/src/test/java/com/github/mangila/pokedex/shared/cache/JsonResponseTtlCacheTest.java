package com.github.mangila.pokedex.shared.cache;

import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class JsonResponseTtlCacheTest {

    @Test
    void shouldPutKeyAndRemoveAfterTtl() {
        var config = new ResponseTtlCacheConfig(
                Duration.ofSeconds(3),
                0,
                3,
                SECONDS
        );
        var cache = new ResponseTtlCache(config);
        cache.put("key", new JsonResponse(null, null, null));
        assertThat(cache.hasKey("key")).isTrue();
        await()
                .atMost(5, SECONDS)
                .until(() -> !cache.hasKey("key"));
    }

    @Test
    void shouldShutdownEvictionThreadAndNotRemoveKey() throws InterruptedException {
        var config = new ResponseTtlCacheConfig(
                Duration.ofSeconds(3),
                0,
                3,
                SECONDS
        );
        var cache = new ResponseTtlCache(config);
        cache.put("key", new JsonResponse(null, null, null));
        assertThat(cache.hasKey("key")).isTrue();
        cache.shutdownEvictionThread();
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);
        assertThat(cache.isShutdownAndTerminated()).isTrue();
        assertThat(cache.hasKey("key")).isTrue();
    }
}