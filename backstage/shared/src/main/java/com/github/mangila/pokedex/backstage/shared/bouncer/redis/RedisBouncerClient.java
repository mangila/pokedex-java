package com.github.mangila.pokedex.backstage.shared.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.service.StreamOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.service.ValueOperationGrpc;
import org.springframework.stereotype.Service;

@Service
public class RedisBouncerClient {

    private final DefaultValueOperation valueOps;
    private final DefaultStreamOperation streamOps;

    public RedisBouncerClient(ValueOperationGrpc.ValueOperationBlockingStub valueOperationBlockingStub,
                              StreamOperationGrpc.StreamOperationBlockingStub streamOperationBlockingStub,
                              StreamOperationGrpc.StreamOperationStub streamOperationStub) {
        this.valueOps = new DefaultValueOperation(valueOperationBlockingStub);
        this.streamOps = new DefaultStreamOperation(streamOperationBlockingStub, streamOperationStub);
    }

    public ValueOperation valueOps() {
        return valueOps;
    }

    public StreamOperation streamOps() {
        return streamOps;
    }
}
