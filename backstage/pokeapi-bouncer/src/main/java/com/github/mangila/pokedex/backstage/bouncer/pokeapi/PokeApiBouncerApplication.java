package com.github.mangila.pokedex.backstage.bouncer.pokeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.github.mangila.pokedex.backstage"})
public class PokeApiBouncerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokeApiBouncerApplication.class, args);
    }
}
