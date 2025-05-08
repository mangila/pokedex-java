package com.github.mangila.pokedex.shared.config;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigLoader {

    private static final Properties properties = loadProperties();

    public static HttpConfig httpConfig() {
        return new HttpConfig();
    }

    public static SocketConfig socketConfig() {
        String keepAlive = properties.getProperty("socket.keepAlive", "true");
        String sendBufferSize = properties.getProperty("socket.sendBufferSize", "8192");
        String receiveBufferSize = properties.getProperty("socket.receiveBufferSize", "1048576");
        String soTimeoutMillis = properties.getProperty("socket.soTimeoutMillis", "10000");
        String soLinger = properties.getProperty("socket.soLinger", "true");
        String soLingerTimeSeconds = properties.getProperty("socket.soLingerTimeSeconds", "1");
        String tcpNoDelay = properties.getProperty("socket.tcpNoDelay", "true");
        return new SocketConfig(
                Boolean.parseBoolean(keepAlive),
                Integer.parseInt(sendBufferSize),
                Integer.parseInt(receiveBufferSize),
                Integer.parseInt(soTimeoutMillis),
                Boolean.parseBoolean(soLinger),
                Integer.parseInt(soLingerTimeSeconds),
                Boolean.parseBoolean(tcpNoDelay)
        );
    }

    public static TlsConfig tlsConfig() {
        String enabledProtocols = properties.getProperty("tls.enabledProtocols", "TLSv1.3");
        String applicationProtocols = properties.getProperty("tls.applicationProtocols", "http/1.1");
        return new TlsConfig(
                new String[]{enabledProtocols},
                new String[]{applicationProtocols}
        );
    }


    private static Properties loadProperties() {
        try {
            Properties properties = new Properties();
            try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
                properties.load(input);
            }
            return properties;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
