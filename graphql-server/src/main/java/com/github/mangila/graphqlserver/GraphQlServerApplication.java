package com.github.mangila.graphqlserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.github.mangila"})
public class GraphQlServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphQlServerApplication.class, args);
    }

}
