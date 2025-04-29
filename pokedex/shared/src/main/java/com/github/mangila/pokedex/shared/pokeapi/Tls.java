package com.github.mangila.pokedex.shared.pokeapi;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Tls {

    public static final SSLContext CONTEXT;

    static {
        /*
        *** ClientHello, [cipher suites and supported SSL/TLS versions]
        *** ServerHello, [cipher suite, session ID, server certificate]
        ...
        *** Certificate verification
        ...
        *** Finished handshake
         */
        System.setProperty("javax.net.debug", "ssl:handshake");
        try {
            CONTEXT = SSLContext.getInstance("TLS");
            // Default Java Keystore with some well-known certificates
            CONTEXT.init(null, null, SecureRandom.getInstanceStrong());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}
