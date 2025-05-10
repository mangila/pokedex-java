package com.github.mangila.pokedex.shared.tls.config;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public record TlsConnectionPoolConfig(String host, int port, int maxConnections, HealthCheckConfig healthCheckConfig) {

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
