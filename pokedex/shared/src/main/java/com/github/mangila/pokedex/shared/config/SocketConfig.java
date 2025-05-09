package com.github.mangila.pokedex.shared.config;

import java.time.Duration;

public record SocketConfig(
        KeepAlive keepAlive,
        BufferSize bufferSize,
        SoTimeout soTimeout,
        SoLinger soLinger,
        TcpNoDelay tcpNoDelay) {

    public record KeepAlive(boolean active) {
    }

    public record BufferSize(int send, int receive) {
        public BufferSize {
            if (send < 0) {
                throw new IllegalArgumentException("send must be greater than or equal to 0");
            }
            if (receive < 0) {
                throw new IllegalArgumentException("receive must be greater than or equal to 0");
            }
        }
    }

    public record SoTimeout(Duration duration) {
        public SoTimeout {
            if (duration.isNegative()) {
                throw new IllegalArgumentException("duration must be greater than or equal to 0");
            }
        }
    }

    public record SoLinger(boolean active, int seconds) {
        public SoLinger {
            if (seconds < 0) {
                throw new IllegalArgumentException("seconds must be greater than or equal to 0");
            }
        }
    }

    public record TcpNoDelay(boolean active) {
    }
}
