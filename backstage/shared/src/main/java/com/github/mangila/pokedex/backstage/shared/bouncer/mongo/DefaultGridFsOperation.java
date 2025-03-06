package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.GridFsGrpc;

class DefaultGridFsOperation implements GridFs {

    private final GridFsGrpc.GridFsBlockingStub stub;

    DefaultGridFsOperation(GridFsGrpc.GridFsBlockingStub stub) {
        this.stub = stub;
    }
}
