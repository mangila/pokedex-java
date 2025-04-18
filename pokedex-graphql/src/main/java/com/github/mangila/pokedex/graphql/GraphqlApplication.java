package com.github.mangila.pokedex.graphql;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@SpringBootApplication
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
