package com.github.mangila.pokedex.shared.https.tls;

import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.util.Ensure;

public record TlsConnectionPoolConfig(String host,
                                      BlockingQueue queue,
                                      int port) {
    public TlsConnectionPoolConfig {
        Ensure.notNull(host, "host property must not be null");
        Ensure.notNull(queue, "queue property must not be null");
        Ensure.min(1, port);
    }
}
