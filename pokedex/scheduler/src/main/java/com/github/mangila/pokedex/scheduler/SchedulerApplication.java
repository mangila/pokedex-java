package com.github.mangila.pokedex.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.github.mangila.pokedex.scheduler", "com.github.mangila.pokedex.shared"})
@EnableMongoRepositories(basePackages = {"com.github.mangila.pokedex.shared.repository"})
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }

}
