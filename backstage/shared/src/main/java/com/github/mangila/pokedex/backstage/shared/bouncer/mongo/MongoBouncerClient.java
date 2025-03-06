package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.MongoDbOperationGrpc;
import org.springframework.stereotype.Service;

@Service
public class MongoBouncerClient {

    private final DefaultMongoDbOperation mongoDb;
    private final DefaultGridFsOperation gridFs;

    public MongoBouncerClient(MongoDbOperationGrpc.MongoDbOperationBlockingStub mongoDbOperationBlockingStub) {
        this.mongoDb = new DefaultMongoDbOperation(mongoDbOperationBlockingStub);
        this.gridFs = new DefaultGridFsOperation();
    }

    public MongoDb mongoDb() {
        return mongoDb;
    }

    public GridFs gridFs() {
        return gridFs;
    }
}
