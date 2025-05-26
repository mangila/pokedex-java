package com.github.mangila.pokedex.shared.queue;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoundedQueueServiceTest {

    @Test
    void shouldCreateAndPollQueue() throws InterruptedException {
        var queueService = BoundedQueueService.getInstance();
        queueService.createNewBoundedQueue("test-queue", 1);
        assertThat(queueService.isEmpty("test-queue"))
                .isTrue();
        queueService.add("test-queue", new QueueEntry("string"));
        var poll = queueService.poll("test-queue", Duration.ofSeconds(1));
        assertThat(poll)
                .isNotEmpty()
                .map(QueueEntry::failCounter)
                .map(AtomicInteger::get)
                .contains(0);
        assertThat(poll)
                .isNotEmpty()
                .map(QueueEntry::data)
                .contains("string");
    }

    @Test
    void shouldThrowExceptionWhenQueueIsFull() throws InterruptedException {
        var queueService = BoundedQueueService.getInstance();
        queueService.createNewBoundedQueue("test-queue", 1);
        queueService.add("test-queue", new QueueEntry("string"));
        assertThatThrownBy(() -> queueService.add("test-queue", new QueueEntry("string")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Queue full");
    }

}