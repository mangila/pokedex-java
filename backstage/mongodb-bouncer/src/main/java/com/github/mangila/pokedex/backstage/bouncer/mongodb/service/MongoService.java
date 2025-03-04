package com.github.mangila.pokedex.backstage.bouncer.mongodb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.InsertRequest;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.MongoDbOperationGrpc;
import com.github.mangila.pokedex.backstage.shared.util.JsonUtil;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class MongoService extends MongoDbOperationGrpc.MongoDbOperationImplBase {

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public MongoService(MongoTemplate mongoTemplate,
                        ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void insertOne(InsertRequest request, StreamObserver<Empty> responseObserver) {
        try {
            var clazz = Class.forName(request.getType());
            var document = JsonUtil.readValueFrom(request.getData(), objectMapper, clazz);
            mongoTemplate.save(document, request.getCollection());
            responseObserver.onNext(Empty.getDefaultInstance());
        } catch (ClassNotFoundException e) {
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }
}
