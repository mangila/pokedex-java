package com.github.mangila.pokedex.shared.tls.config;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public record TlsConnectionPoolConfig(String host,
                                      int port,
                                      PoolConfig poolConfig,
                                      HealthCheckConfig healthCheckConfig) {

    public record PoolConfig(String poolName, int maxConnections) {
        public PoolConfig {
            Objects.requireNonNull(poolName, "poolName must not be null");
            if (maxConnections <= 0) {
                throw new IllegalArgumentException("maxConnections must be greater than 0");
            }
        }
    }

    public record HealthCheckConfig(int initialDelay, int delay, TimeUnit timeUnit) {

        public HealthCheckConfig {
            if (initialDelay < 0) {
                throw new IllegalArgumentException("initialDelay must be greater than 0");
            }
            if (delay <= 0) {
                throw new IllegalArgumentException("delay must be greater than 0");
            }
            Objects.requireNonNull(timeUnit, "timeUnit must not be null");
        }

    }

}
