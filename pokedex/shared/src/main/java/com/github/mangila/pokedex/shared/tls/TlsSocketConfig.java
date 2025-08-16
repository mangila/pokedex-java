package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.time.Duration;

public record TlsSocketConfig(
        KeepAlive keepAlive,
        BufferSize bufferSize,
        SoTimeout soTimeout,
        SoLinger soLinger,
        TcpNoDelay tcpNoDelay) {

    public record KeepAlive(boolean active) {
    }

    public record BufferSize(int send, int receive) {
        public BufferSize {
            Ensure.min(1, send);
            Ensure.min(1, receive);
        }
    }

    public record SoTimeout(Duration duration) {
        public SoTimeout {
            if (duration.isNegative()) {
                throw new IllegalArgumentException("duration must not be negative");
            }
        }
    }

    public record SoLinger(boolean active, int seconds) {
        public SoLinger {
            Ensure.min(1, seconds);
        }
    }

    public record TcpNoDelay(boolean active) {
    }
}
