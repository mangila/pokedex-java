package com.github.mangila.pokedex.shared.tls.pool;

import com.github.mangila.pokedex.shared.tls.TlsConnection;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Runs in the background periodically - reconnects disconnected sockets
 */
public class HealthProbe implements Runnable {

    private final ConcurrentLinkedQueue<TlsConnection> queue;

    public HealthProbe(ConcurrentLinkedQueue<TlsConnection> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        queue.forEach(TlsConnection::reconnectIfUnHealthy);
    }
}
