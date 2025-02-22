package com.github.mangila.pokedex.backstage.bouncer.mongodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.github.mangila.pokedex.backstage"})
public class MongoDbBouncerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoDbBouncerApplication.class, args);
    }
}
