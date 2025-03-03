package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public interface StreamOperation {

    StreamRecord readOne(StreamRecord request);

    Empty acknowledgeOne(StreamRecord request);

    StreamObserver<StreamRecord> addWithClientStream(StreamObserver<Empty> responseObserver);
}
