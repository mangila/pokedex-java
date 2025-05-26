package com.github.mangila.pokedex.shared.queue;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class QueueServiceTest {

    @Test
    void shouldCreateAndPollQueue() {
        var queueService = QueueService.getInstance();
        queueService.createNewQueue("test-queue");
        assertThat(queueService.isEmpty("test-queue"))
                .isTrue();
        queueService.add("test-queue", new QueueEntry("string"));
        assertThat(queueService.isEmpty("test-queue"))
                .isFalse();
        var poll = queueService.poll("test-queue");
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

}