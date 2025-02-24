package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

class DefaultStreamOperation implements StreamOperation {

    private final StreamOperationGrpc.StreamOperationStub streamOperationStub;

    protected DefaultStreamOperation(StreamOperationGrpc.StreamOperationStub streamOperationStub) {
        this.streamOperationStub = streamOperationStub;
    }

    @Override
    public StreamObserver<StreamRecord> addWithClientStream(StreamObserver<Empty> responseObserver) {
        return streamOperationStub.addWithClientStream(responseObserver);
    }
}
