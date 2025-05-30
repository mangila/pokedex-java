package com.github.mangila.pokedex.shared.cache.ttl;

import com.github.mangila.pokedex.shared.https.model.Headers;
import com.github.mangila.pokedex.shared.https.model.HttpStatus;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.json.model.JsonTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TtlCacheTest {

    @Test
    @DisplayName("Should put key and remove after TTL")
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
    @DisplayName("Should get value from cache")
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

    @Test
    @DisplayName("Should return null when key not found")
    void shouldReturnNullWhenKeyNotFound() {
        // Given
        var config = new TtlCacheConfig(
                Duration.ofSeconds(10),
                new TtlCacheConfig.EvictionConfig(0, 3, SECONDS)
        );
        var cache = new TtlCache<String, JsonResponse>(config);

        // When/Then
        assertThat(cache.get("nonexistent")).isNull();
        assertThat(cache.hasKey("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should update existing key with new value")
    void shouldUpdateExistingKeyWithNewValue() {
        // Given
        var config = new TtlCacheConfig(
                Duration.ofSeconds(10),
                new TtlCacheConfig.EvictionConfig(0, 3, SECONDS)
        );
        var cache = new TtlCache<String, JsonResponse>(config);
        var httpStatus1 = new HttpStatus("HTTP/1.1", "200", "OK");
        var headers1 = new Headers();
        var jsonTree1 = new JsonTree();
        var response1 = new JsonResponse(httpStatus1, headers1, jsonTree1);

        var httpStatus2 = new HttpStatus("HTTP/1.1", "201", "Created");
        var headers2 = new Headers();
        var jsonTree2 = new JsonTree();
        var response2 = new JsonResponse(httpStatus2, headers2, jsonTree2);

        // When
        cache.put("key", response1);
        cache.put("key", response2);

        // Then
        assertThat(cache.get("key")).isEqualTo(response2);
    }

    @Test
    @DisplayName("Should shutdown eviction thread")
    void shouldShutdownEvictionThread() {
        // Given
        var config = new TtlCacheConfig(
                Duration.ofSeconds(10),
                new TtlCacheConfig.EvictionConfig(0, 3, SECONDS)
        );
        var cache = new TtlCache<String, JsonResponse>(config);

        // When
        cache.shutdownEvictionThread();

        // Then
        assertThat(cache.isEvictionThreadShutdown()).isTrue();
    }

    @Test
    @DisplayName("Should check if eviction thread is shutdown")
    void shouldCheckIfEvictionThreadIsShutdown() {
        // Given
        var config = new TtlCacheConfig(
                Duration.ofSeconds(10),
                new TtlCacheConfig.EvictionConfig(0, 3, SECONDS)
        );
        var cache = new TtlCache<String, JsonResponse>(config);

        // When/Then
        assertThat(cache.isEvictionThreadShutdown()).isFalse();
    }

    @Test
    @DisplayName("Should create cache with default config")
    void shouldCreateCacheWithDefaultConfig() {
        // Given
        var config = TtlCacheConfig.fromDefaultConfig();

        // When
        var cache = new TtlCache<String, String>(config);

        // Then
        assertThat(cache).isNotNull();
        assertThat(cache.isEvictionThreadShutdown()).isFalse();
    }

    @Nested
    @DisplayName("TtlCacheConfig Tests")
    class TtlCacheConfigTest {

        @Test
        @DisplayName("Should throw exception when ttl is null")
        void shouldThrowExceptionWhenTtlIsNull() {
            // When/Then
            assertThrows(NullPointerException.class, () -> 
                new TtlCacheConfig(null, new TtlCacheConfig.EvictionConfig(0, 3, SECONDS)));
        }

        @Test
        @DisplayName("Should throw exception when evictionConfig is null")
        void shouldThrowExceptionWhenEvictionConfigIsNull() {
            // When/Then
            assertThrows(NullPointerException.class, () -> 
                new TtlCacheConfig(Duration.ofSeconds(10), null));
        }

        @Test
        @DisplayName("Should throw exception when initialDelay is negative")
        void shouldThrowExceptionWhenInitialDelayIsNegative() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> 
                new TtlCacheConfig.EvictionConfig(-1, 3, SECONDS));
        }

        @Test
        @DisplayName("Should throw exception when delay is negative")
        void shouldThrowExceptionWhenDelayIsNegative() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> 
                new TtlCacheConfig.EvictionConfig(0, -1, SECONDS));
        }

        @Test
        @DisplayName("Should throw exception when timeUnit is null")
        void shouldThrowExceptionWhenTimeUnitIsNull() {
            // When/Then
            assertThrows(NullPointerException.class, () -> 
                new TtlCacheConfig.EvictionConfig(0, 3, null));
        }

        @Test
        @DisplayName("Should create config with valid parameters")
        void shouldCreateConfigWithValidParameters() {
            // When
            var config = new TtlCacheConfig(
                    Duration.ofSeconds(10),
                    new TtlCacheConfig.EvictionConfig(0, 3, SECONDS)
            );

            // Then
            assertThat(config.ttl()).isEqualTo(Duration.ofSeconds(10));
            assertThat(config.evictionConfig().initialDelay()).isEqualTo(0);
            assertThat(config.evictionConfig().delay()).isEqualTo(3);
            assertThat(config.evictionConfig().timeUnit()).isEqualTo(SECONDS);
        }
    }
}
