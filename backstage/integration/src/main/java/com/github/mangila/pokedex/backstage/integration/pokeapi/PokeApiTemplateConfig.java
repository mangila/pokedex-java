package com.github.mangila.pokedex.backstage.integration.pokeapi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class PokeApiTemplateConfig {

    private final PokeApiTemplateProps pokeApiTemplateProps;

    public PokeApiTemplateConfig(PokeApiTemplateProps pokeApiTemplateProps) {
        this.pokeApiTemplateProps = pokeApiTemplateProps;
    }

    @Bean("pokeApiClient")
    @ConditionalOnProperty(name = "app.integration.pokeapi")
    public RestClient pokeApiClient() {
        return RestClient.builder()
                .baseUrl(pokeApiTemplateProps.getHost())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                .defaultHeader(HttpHeaders.USER_AGENT, "pokedex-spring-boot")
                .build();
    }
}
