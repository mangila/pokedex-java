package com.github.mangila.pokedex.backstage.shared.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class PokeApiTemplateConfig {

    @Bean("pokeApiClient")
    public RestClient pokeApiClient() {
        return RestClient.builder()
                .baseUrl("https://pokeapi.co/api/v2")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                .defaultHeader(HttpHeaders.USER_AGENT, "pokedex-spring-boot")
                .build();
    }
}
