package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;


import com.github.mangila.pokedex.backstage.model.grpc.model.MediaValue;
import com.github.mangila.pokedex.backstage.model.grpc.service.GridFsGrpc;
import com.google.protobuf.StringValue;

class DefaultGridFsOperation implements GridFs {

    private final GridFsGrpc.GridFsBlockingStub stub;

    DefaultGridFsOperation(GridFsGrpc.GridFsBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public StringValue storeOne(MediaValue request) {
        return stub.storeOne(request);
    }
}
