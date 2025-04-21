package com.github.mangila.pokedex.graphql;

import org.springframework.boot.SpringApplication;

public class TestPokedexGraphqlApplication {

    public static void main(String[] args) {
        SpringApplication.from(GraphqlApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }

}
