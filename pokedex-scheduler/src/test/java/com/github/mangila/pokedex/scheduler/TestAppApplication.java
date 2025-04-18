package com.github.mangila.pokedex.scheduler;

import org.springframework.boot.SpringApplication;

public class TestAppApplication {

    public static void main(String[] args) {
        SpringApplication.from(SchedulerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
