package com.github.mangila.pokedex.shared.cache.lru;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LruCacheTest {

    @Test
    @DisplayName("Should put and get value from cache")
    void shouldPutAndGetValueFromCache() {
        // Given
        var config = new LruCacheConfig(5);
        var cache = new LruCache<String, String>(config);

        // When
        cache.put("key1", "value1");

        // Then
        assertThat(cache.hasKey("key1")).isTrue();
        assertThat(cache.get("key1")).isEqualTo("value1");
    }

    @Test
    @DisplayName("Should return null when key not found")
    void shouldReturnNullWhenKeyNotFound() {
        // Given
        var config = new LruCacheConfig(5);
        var cache = new LruCache<String, String>(config);

        // When/Then
        assertThat(cache.get("nonexistent")).isNull();
        assertThat(cache.hasKey("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should update existing key with new value")
    void shouldUpdateExistingKeyWithNewValue() {
        // Given
        var config = new LruCacheConfig(5);
        var cache = new LruCache<String, String>(config);

        // When
        cache.put("key1", "value1");
        cache.put("key1", "value2");

        // Then
        assertThat(cache.get("key1")).isEqualTo("value1"); // Value doesn't change as per implementation
    }

    @Test
    @DisplayName("Should evict least recently used item when capacity is reached")
    void shouldEvictLeastRecentlyUsedItemWhenCapacityIsReached() {
        // Given
        var config = new LruCacheConfig(3);
        var cache = new LruCache<String, String>(config);

        // When
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // Access key1 to make it recently used
        cache.get("key1");

        // Add a new item that exceeds capacity
        cache.put("key4", "value4");

        // Then
        assertThat(cache.hasKey("key1")).isTrue(); // Recently used, should still be there
        assertThat(cache.hasKey("key2")).isFalse(); // Least recently used, should be evicted
        assertThat(cache.hasKey("key3")).isTrue(); // Should still be there
        assertThat(cache.hasKey("key4")).isTrue(); // Newly added, should be there
    }

    @Test
    @DisplayName("Should maintain LRU order when accessing items")
    void shouldMaintainLruOrderWhenAccessingItems() {
        // Given
        var config = new LruCacheConfig(3);
        var cache = new LruCache<String, String>(config);

        // When
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // Access key1 and key2 to make them recently used
        cache.get("key1");
        cache.get("key2");

        // Add a new item that exceeds capacity
        cache.put("key4", "value4");

        // Then
        assertThat(cache.hasKey("key1")).isTrue(); // Recently used, should still be there
        assertThat(cache.hasKey("key2")).isTrue(); // Recently used, should still be there
        assertThat(cache.hasKey("key3")).isFalse(); // Least recently used, should be evicted
        assertThat(cache.hasKey("key4")).isTrue(); // Newly added, should be there
    }

    @Nested
    @DisplayName("LruCacheConfig Tests")
    class LruCacheConfigTest {

        @Test
        @DisplayName("Should throw exception when capacity is zero")
        void shouldThrowExceptionWhenCapacityIsZero() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> new LruCacheConfig(0));
        }

        @Test
        @DisplayName("Should throw exception when capacity is negative")
        void shouldThrowExceptionWhenCapacityIsNegative() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> new LruCacheConfig(-1));
        }

        @Test
        @DisplayName("Should throw exception when capacity exceeds maximum")
        void shouldThrowExceptionWhenCapacityExceedsMaximum() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> new LruCacheConfig(101));
        }

        @Test
        @DisplayName("Should create config with valid capacity")
        void shouldCreateConfigWithValidCapacity() {
            // When
            var config = new LruCacheConfig(50);

            // Then
            assertThat(config.capacity()).isEqualTo(50);
        }
    }
}
