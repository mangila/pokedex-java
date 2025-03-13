package com.github.mangila.pokedex.backstage.shared.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.model.StreamRecord;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public interface StreamOperation {

    StreamRecord readOne(StreamRecord request);

    Empty acknowledgeOne(StreamRecord request);

    Empty addOne(StreamRecord request);

    StreamObserver<StreamRecord> addWithClientStream(StreamObserver<Empty> responseObserver);
}
