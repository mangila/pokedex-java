package com.github.mangila.pokedex.graphql;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.github.mangila.pokedex.graphql", "com.github.mangila.pokedex.shared"})
@EnableMongoRepositories(basePackages = {"com.github.mangila.pokedex.shared.repository"})
@lombok.AllArgsConstructor
public class GraphqlApplication {

    private final MongoTemplate mongoTemplate;

    public static void main(String[] args) {
        SpringApplication.run(GraphqlApplication.class, args);
    }

    @PostConstruct
    public void initMongoDbIndexes() {
        mongoTemplate.indexOps("pokemon.files")
                .ensureIndex(new Index().on("filename", Sort.Direction.ASC));
    }

}
