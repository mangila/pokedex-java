package com.github.mangila.pokedex.shared.queue;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueueEntryTest {

    @Test
    void shouldCreateQueueEntryWithData() {
        // Given
        String testData = "test data";

        // When
        var queueEntry = new QueueEntry(testData);

        // Then
        assertThat(queueEntry.data()).isEqualTo(testData);
        assertThat(queueEntry.failCounter().get()).isZero();
    }

    @Test
    void shouldIncrementFailCounter() {
        // Given
        var queueEntry = new QueueEntry("test data");

        // When
        queueEntry.incrementFailCounter();
        queueEntry.incrementFailCounter();

        // Then
        assertThat(queueEntry.failCounter().get()).isEqualTo(2);
    }

    @Test
    void shouldCheckIfEqualsMaxRetries() {
        // Given
        var queueEntry = new QueueEntry("test data");
        int maxRetries = 3;

        // When
        for (int i = 0; i < maxRetries; i++) {
            queueEntry.incrementFailCounter();
        }

        // Then
        assertThat(queueEntry.equalsMaxRetries(maxRetries)).isTrue();
        assertThat(queueEntry.equalsMaxRetries(maxRetries + 1)).isFalse();
    }

    @Test
    void shouldCastDataToSpecificType() {
        // Given
        Integer testData = 42;
        var queueEntry = new QueueEntry(testData);

        // When
        Integer castedData = queueEntry.getDataAs(Integer.class);

        // Then
        assertThat(castedData).isEqualTo(testData);
    }
}
