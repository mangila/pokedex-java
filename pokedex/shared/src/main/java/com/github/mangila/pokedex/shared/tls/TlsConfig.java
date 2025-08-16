package com.github.mangila.pokedex.shared.tls;

import java.util.Objects;

public record TlsConfig(String[] enabledProtocols, String[] applicationProtocols) {

    public TlsConfig {
        Objects.requireNonNull(enabledProtocols, "enabledProtocols must not be null");
        Objects.requireNonNull(applicationProtocols, "applicationProtocols must not be null");
    }

}
