package com.lowtuna.dropwizard.grpc;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;

/**
 * Created by tburch on 3/20/17.
 */
public class FooServiceImpl extends FooServiceGrpc.FooServiceImplBase {
  @Override
  public void doFoo(FooRequest request, StreamObserver<FooResponse> responseObserver) {
    if (StringUtils.isEmpty(request.getBar())) {
      StatusException exception = Status.FAILED_PRECONDITION.withDescription("Bar was not given").asException();
      responseObserver.onError(exception);
    } else {
      FooResponse response = FooResponse.newBuilder()
              .setBaz("You said " + request.getBar() + ". Current time is " + Instant.now())
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
  }
}
