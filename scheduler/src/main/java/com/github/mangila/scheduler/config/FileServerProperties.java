package com.github.mangila.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.file-server")
@lombok.Data
public class FileServerProperties {
    private String scheme;
    private String host;
    private Integer port;
    private String uri;
}
