package com.github.mangila.pokedex.backstage.bouncer.mongodb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.InsertRequest;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.MongoDbOperationGrpc;
import com.github.mangila.pokedex.backstage.shared.model.document.PokemonSpeciesDocument;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Disabled(value = "Run only where a Docker env is available")
class MongoServiceTest extends MongoDbTestContainer {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void insertOne() {
        var channel = ManagedChannelBuilder.forAddress("0.0.0.0", 9010)
                .usePlaintext()
                .build();
        var stub = MongoDbOperationGrpc.newBlockingStub(channel);
        var document = TestDataGenerator.createPokemonSpeciesDocument();
        stub.insertOne(
                InsertRequest.newBuilder()
                        .setType(PokemonSpeciesDocument.class.getName())
                        .setCollection("pokemon-species")
                        .setData(document.toJson(objectMapper))
                        .build()
        );
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is("bulbasaur"));
        var findOne = mongoTemplate.findOne(query, PokemonSpeciesDocument.class);
        assertThat(findOne.name()).isEqualTo("bulbasaur");
    }
}