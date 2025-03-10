package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.service.GridFsGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.service.MongoDbGrpc;
import org.springframework.stereotype.Service;

@Service
public class MongoBouncerClient {

    private final DefaultMongoDbOperation mongoDb;
    private final DefaultGridFsOperation gridFs;

    public MongoBouncerClient(MongoDbGrpc.MongoDbBlockingStub mongoDbBlockingStub,
                              GridFsGrpc.GridFsBlockingStub gridFsBlockingStub) {
        this.mongoDb = new DefaultMongoDbOperation(mongoDbBlockingStub);
        this.gridFs = new DefaultGridFsOperation(gridFsBlockingStub);
    }

    public MongoDb mongoDb() {
        return mongoDb;
    }

    public GridFs gridFs() {
        return gridFs;
    }
}
