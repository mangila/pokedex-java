package com.github.mangila.pokedex.shared.cache;

import com.github.mangila.pokedex.shared.https.model.Headers;
import com.github.mangila.pokedex.shared.https.model.HttpStatus;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.json.model.JsonTree;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ResponseTtlCacheTest {

    @Test
    void shouldPutKeyAndRemoveAfterTtl() {
        var config = new ResponseTtlCacheConfig(
                Duration.ofSeconds(3),
                0,
                3,
                SECONDS
        );
        var cache = new ResponseTtlCache(config);
        cache.startEvictionThread();
        cache.put("key", new JsonResponse(HttpStatus.fromString("HTTP 200 OK"),
                new Headers(),
                new JsonTree()));
        assertThat(cache.hasKey("key")).isTrue();
        await()
                .atMost(10, SECONDS)
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
        cache.startEvictionThread();
        cache.put("key", new JsonResponse(HttpStatus.fromString("HTTP 200 OK"),
                Collections.emptyMap(),
                new JsonTree()));
        assertThat(cache.hasKey("key")).isTrue();
        cache.shutdownEvictionThread();
        SECONDS.sleep(5);
        assertThat(cache.isShutdownAndTerminated()).isTrue();
        assertThat(cache.hasKey("key")).isTrue();
    }
}