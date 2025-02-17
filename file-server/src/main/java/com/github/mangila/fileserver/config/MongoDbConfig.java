package com.github.mangila.fileserver.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("com.github.mangila.repository")
@AllArgsConstructor
@Slf4j
public class MongoDbConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        log.info("init index on pokemon.files - filename");
        mongoTemplate
                .indexOps("pokemon.files")
                .ensureIndex(new Index("filename", Sort.Direction.ASC));
    }
}
