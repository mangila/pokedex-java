package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.HelloRequest;
import com.github.mangila.pokedex.backstage.model.grpc.SimpleGrpc;
import org.springframework.stereotype.Service;

@Service
public class RedisBouncerClient {

    private final SimpleGrpc.SimpleBlockingStub grpcClient;

    public RedisBouncerClient(SimpleGrpc.SimpleBlockingStub grpcClient) {
        this.grpcClient = grpcClient;
    }

    public void abc() {
      var l =  grpcClient.sayHello(
                HelloRequest.newBuilder().build()
        );
        System.out.println(l.getMessage());
    }
}
