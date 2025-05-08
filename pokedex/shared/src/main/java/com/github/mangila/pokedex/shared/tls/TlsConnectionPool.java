package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;

import java.util.concurrent.ScheduledExecutorService;

public class TlsConnectionPool {

    private final ScheduledExecutorService healthProbe = VirtualThreadConfig.newSingleThreadScheduledExecutor();

    public TlsConnectionPool() {
        
    }

}
