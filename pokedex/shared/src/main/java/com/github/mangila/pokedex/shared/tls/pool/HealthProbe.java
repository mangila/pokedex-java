package com.github.mangila.pokedex.shared.tls.pool;

import com.github.mangila.pokedex.shared.tls.TlsConnection;

import java.util.concurrent.BlockingQueue;

/**
 * Run a background virtual thread that checks health status and reconnects
 * if unhealthy
 */
public class HealthProbe implements Runnable {

    private final BlockingQueue<TlsConnection> queue;

    public HealthProbe(BlockingQueue<TlsConnection> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        queue.forEach(TlsConnection::reconnectIfUnHealthy);
    }
}
