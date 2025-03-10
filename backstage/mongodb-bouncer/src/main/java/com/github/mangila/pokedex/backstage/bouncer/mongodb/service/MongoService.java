package com.github.mangila.pokedex.backstage.bouncer.mongodb.service;

import com.github.mangila.pokedex.backstage.bouncer.mongodb.mapper.DocumentMapper;
import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonSpecies;
import com.github.mangila.pokedex.backstage.model.grpc.service.MongoDbGrpc;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class MongoService extends MongoDbGrpc.MongoDbImplBase {

    private final MongoTemplate mongoTemplate;
    private final DocumentMapper documentMapper;

    public MongoService(MongoTemplate mongoTemplate,
                        DocumentMapper documentMapper) {
        this.mongoTemplate = mongoTemplate;
        this.documentMapper = documentMapper;
    }

    @Override
    public void saveOne(PokemonSpecies request, StreamObserver<Empty> responseObserver) {
        mongoTemplate.save(documentMapper.toDocument(request));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
