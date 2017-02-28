package com.lowtuna.dropwizard.grpc.example;

import com.lowtuna.dropwizard.grpc.examples.helloworld.GreeterGrpc;
import com.lowtuna.dropwizard.grpc.examples.helloworld.HelloReply;
import com.lowtuna.dropwizard.grpc.examples.helloworld.HelloRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by tburch on 2/28/17.
 */
@Slf4j
public class GreeterClient {

  public static void main(String[] args) throws Exception {
    String clientArgs[] = Arrays.copyOf(args, 3);
    String name = Optional.ofNullable(clientArgs[0]).orElse("Tristan");
    String host = Optional.ofNullable(clientArgs[1]).orElse("localhost");
    String portString = Optional.ofNullable(clientArgs[2]).orElse("9000");
    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, Integer.parseInt(portString))
            .usePlaintext(true)
            .build();

    GreeterGrpc.GreeterBlockingStub greeterClient = GreeterGrpc.newBlockingStub(channel);

    HelloRequest helloRequest = HelloRequest.newBuilder()
            .setName(name)
            .build();
    log.info("Sending HelloRequest with name {}", name);
    HelloReply helloReply = greeterClient.sayHello(helloRequest);
    log.info("Got HelloReply with message: {}", helloReply.getMessage());

    log.info("Sending HelloRequest with no name");
    try {
      greeterClient.sayHello(HelloRequest.newBuilder().build());
    } catch (StatusRuntimeException e) {
      log.info("Caught StatusRuntimeException with status {} and message", e.getStatus(), e.getMessage());
    }
  }

}
