package com.github.mangila.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("com.github.mangila.repository")
public class MongoDbConfig {
}
