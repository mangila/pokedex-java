package com.github.mangila.pokedex.shared.queue;

public class QueueNotFoundException extends RuntimeException {
    public QueueNotFoundException(QueueName queueName) {
        super(queueName.value());
    }
}
