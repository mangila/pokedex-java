package com.github.mangila.pokedex.shared.queue;

import com.github.mangila.pokedex.shared.util.Ensure;

public record QueueName(String value) {

    public QueueName {
        Ensure.notNull(value, "Queue name must not be null");
        Ensure.notBlank(value, "Queue name must not be blank");
        Ensure.min(1, value.length());
        Ensure.max(255, value.length());
    }

}
