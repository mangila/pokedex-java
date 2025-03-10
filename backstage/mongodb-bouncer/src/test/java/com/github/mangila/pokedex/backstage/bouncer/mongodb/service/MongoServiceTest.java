package com.github.mangila.pokedex.backstage.bouncer.mongodb.service;

import com.github.mangila.pokedex.backstage.bouncer.mongodb.document.PokemonSpeciesDocument;
import com.github.mangila.pokedex.backstage.model.grpc.service.MongoDbGrpc;
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

//    @Test
//    void saveOne() {
//        var channel = ManagedChannelBuilder.forAddress("0.0.0.0", 9010)
//                .usePlaintext()
//                .build();
//        var stub = MongoDbGrpc.newBlockingStub(channel);
//        var document = TestDataGenerator.createDefaultPokemonSpeciesDocument();
//        stub.saveOne(document.toProto());
//        Query query = new Query();
//        query.addCriteria(Criteria.where("name").is("bulbasaur"));
//        var findOne = mongoTemplate.findOne(query, PokemonSpeciesDocument.class);
//        assertThat(findOne.name()).isEqualTo("bulbasaur");
//    }
}