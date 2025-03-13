package com.github.mangila.pokedex.backstage.shared.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.model.StreamRecord;
import com.github.mangila.pokedex.backstage.model.grpc.service.StreamOperationGrpc;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

class DefaultStreamOperation implements StreamOperation {

    private final StreamOperationGrpc.StreamOperationBlockingStub streamOperationBlockingStub;
    private final StreamOperationGrpc.StreamOperationStub streamOperationStub;

    public DefaultStreamOperation(StreamOperationGrpc.StreamOperationBlockingStub streamOperationBlockingStub,
                                  StreamOperationGrpc.StreamOperationStub streamOperationStub) {
        this.streamOperationBlockingStub = streamOperationBlockingStub;
        this.streamOperationStub = streamOperationStub;
    }

    @Override
    public StreamRecord readOne(StreamRecord request) {
        return streamOperationBlockingStub.readOne(request);
    }

    @Override
    public Empty acknowledgeOne(StreamRecord request) {
        return streamOperationBlockingStub.acknowledgeOne(request);
    }

    @Override
    public Empty addOne(StreamRecord request) {
        return streamOperationBlockingStub.addOne(request);
    }

    @Override
    public StreamObserver<StreamRecord> addWithClientStream(StreamObserver<Empty> responseObserver) {
        return streamOperationStub.addWithClientStream(responseObserver);
    }
}
