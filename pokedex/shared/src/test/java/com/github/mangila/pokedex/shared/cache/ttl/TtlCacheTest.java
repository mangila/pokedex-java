package com.github.mangila.pokedex.shared.cache.ttl;

import com.github.mangila.pokedex.shared.https.model.Headers;
import com.github.mangila.pokedex.shared.https.model.HttpStatus;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.json.model.JsonTree;
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
                new TtlCacheConfig.EvictionConfig(0, 3, SECONDS)
        );
        var cache = new TtlCache<String, JsonResponse>(config);
        var httpStatus = new HttpStatus("HTTP/1.1", "200", "OK");
        var headers = new Headers();
        var jsonTree = new JsonTree();

        cache.put("key", new JsonResponse(httpStatus, headers, jsonTree));
        assertThat(cache.hasKey("key")).isTrue();
        await()
                .atMost(10, SECONDS)
                .until(() -> !cache.hasKey("key"));
    }

    @Test
    void shouldGetValueFromCache() {
        var config = new TtlCacheConfig(
                Duration.ofSeconds(10),
                new TtlCacheConfig.EvictionConfig(0, 3, SECONDS)
        );
        var cache = new TtlCache<String, JsonResponse>(config);
        var httpStatus = new HttpStatus("HTTP/1.1", "200", "OK");
        var headers = new Headers();
        var jsonTree = new JsonTree();

        var response = new JsonResponse(httpStatus, headers, jsonTree);
        cache.put("key", response);
        assertThat(cache.hasKey("key")).isTrue();
        assertThat(cache.get("key")).isEqualTo(response);
    }
}
