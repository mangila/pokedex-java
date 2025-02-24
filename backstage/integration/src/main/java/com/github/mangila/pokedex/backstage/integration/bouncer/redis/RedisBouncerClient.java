package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.ValueOperationGrpc;
import org.springframework.stereotype.Service;

@Service
public class RedisBouncerClient {

    private final DefaultValueOperation valueOps;
    private final DefaultStreamOperation streamOps;

    public RedisBouncerClient(ValueOperationGrpc.ValueOperationBlockingStub valueOperationBlockingStub,
                              StreamOperationGrpc.StreamOperationStub streamOperationStub) {
        this.valueOps = new DefaultValueOperation(valueOperationBlockingStub);
        this.streamOps = new DefaultStreamOperation(streamOperationStub);
    }

    public ValueOperation valueOps() {
        return valueOps;
    }

    public StreamOperation streamOps() {
        return streamOps;
    }
}
