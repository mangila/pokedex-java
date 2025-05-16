package com.github.mangila.pokedex.shared.queue;

public class QueueNotFoundException extends RuntimeException {
    public QueueNotFoundException(String message) {
        super(message);
    }
}
