package com.lowtuna.dropwizard.grpc.example;

import com.lowtuna.dropwizard.grpc.examples.helloworld.GreeterGrpc;
import com.lowtuna.dropwizard.grpc.examples.helloworld.HelloReply;
import com.lowtuna.dropwizard.grpc.examples.helloworld.HelloRequest;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by tburch on 2/27/17.
 */
@RequiredArgsConstructor
public class GreeterServiceImpl extends GreeterGrpc.GreeterImplBase {
  private final String name;

  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    if (StringUtils.isEmpty(request.getName())) {
      StatusException exception = Status.FAILED_PRECONDITION.withDescription("Name was not given").asException();
      responseObserver.onError(exception);
    } else {
      String message = "Hi " + request.getName() + "! My name is " + name + ". How are you doing today?";
      HelloReply reply = HelloReply.newBuilder()
              .setMessage(message)
              .build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }
  }

}
